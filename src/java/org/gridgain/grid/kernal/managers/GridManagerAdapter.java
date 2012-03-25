// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.managers;

import org.gridgain.grid.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.logger.*;
import org.gridgain.grid.spi.*;
import org.gridgain.grid.typedef.*;
import org.gridgain.grid.typedef.internal.*;
import org.gridgain.grid.util.tostring.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.*;

import static org.gridgain.grid.kernal.managers.communication.GridIoPolicy.*;

/**
 * Convenience adapter for grid managers.
 *
 * @param <T> SPI wrapped by this manager.
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.24032012
 */
public abstract class GridManagerAdapter<T extends GridSpi> implements GridManager {
    /** Kernal context. */
    @GridToStringExclude
    protected final GridKernalContext ctx;

    /** Logger. */
    @GridToStringExclude
    protected final GridLogger log;

    /** Set of SPIs for this manager. */
    @GridToStringExclude
    private final T[] spis;

    /** Set of SPI proxies for this manager. */
    @GridToStringExclude
    private final T[] proxies;

    /** SPI context read-write lock. */
    @GridToStringExclude
    private final ReadWriteLock spiRwLock = new ReentrantReadWriteLock();

    /**
     * @param itf SPI interface.
     * @param ctx Kernal context.
     * @param spis Specific SPI instance.
     */
    @SuppressWarnings("unchecked")
    protected GridManagerAdapter(Class<T> itf, GridKernalContext ctx, T... spis) {
        assert spis != null;
        assert spis.length > 0;
        assert ctx != null;

        this.ctx = ctx;
        this.spis = spis;

        proxies = (T[])Array.newInstance(itf, spis.length);

        for (int i = 0; i < spis.length; i++) {
            final GridSpi spi = this.spis[i];

            // Create proxy SPI to wrap all calls into context real lock.
            // This is done to avoid context destruction during any of the
            // public SPI method invocations.
            proxies[i] = (T)Proxy.newProxyInstance(spis.getClass().getClassLoader(), new Class[]{itf},
                new InvocationHandler() {
                    @SuppressWarnings({"ProhibitedExceptionDeclared", "ProhibitedExceptionThrown"})
                    @Override public Object invoke(Object proxy, Method mtd, Object[] args) throws Throwable {
                        // Wrap all SPI invocations inside of read-lock to
                        // prevent context destruction during SPI invocations.
                        spiRwLock.readLock().lock();

                        try {
                            return mtd.invoke(spi, args);
                        }
                        catch (InvocationTargetException e) {
                            if (e.getCause() != null) {
                                throw e.getCause();
                            }

                            throw e;
                        }
                        finally {
                            spiRwLock.readLock().unlock();
                        }
                    }
                });
        }

        log = ctx.log(getClass());
    }

    /**
     * Gets wrapped SPI.
     *
     * @return Wrapped SPI.
     */
    protected final T getSpi() {
        return proxies[0];
    }

    /**
     * @param name SPI name
     * @return SPI for given name. If {@code null} or empty, then 1st SPI on the list
     *      is returned.
     */
    protected final T getSpi(String name) {
        if (F.isEmpty(name))
            return proxies[0];

        // Loop through SPI's, not proxies, because
        // proxy.getName() is more expensive than spi.getName().
        for (int i = 0; i < spis.length; i++) {
            T t = spis[i];

            if (t.getName().equals(name))
                return proxies[i];
        }

        throw new GridRuntimeException("Failed to find SPI for name: " + name);
    }

    /**
     * @return Configured SPI's.
     */
    protected final T[] getSpis() {
        return spis;
    }

    /**
     * @return Proxy wrappers around SPI's.
     */
    protected final T[] getProxies() {
        return proxies;
    }

    /** {@inheritDoc} */
    @Override public final void addSpiAttributes(Map<String, Object> attrs) throws GridException {
        for (T spi : spis) {
            // Inject all spi resources.
            ctx.resource().inject(spi);

            // Inject SPI internal objects.
            inject(spi);

            try {
                Map<String, Object> retval = spi.getNodeAttributes();

                if (retval != null) {
                    for (Map.Entry<String, Object> e : retval.entrySet()) {
                        if (attrs.containsKey(e.getKey()))
                            throw new GridException("SPI attribute collision for attribute [spi=" + spi +
                                ", attr=" + e.getKey() + ']' +
                                ". Attribute set by one SPI implementation has the same name (name collision) as " +
                                "attribute set by other SPI implementation. Such overriding is not allowed. " +
                                "Please check your GridGain configuration and/or SPI implementation to avoid " +
                                "attribute name collisions.");

                        attrs.put(e.getKey(), e.getValue());
                    }
                }
            }
            catch (GridSpiException e) {
                throw new GridException("Failed to get SPI attributes.", e);
            }
        }
    }

    /**
     * @param spi SPI whose internal objects need to be injected.
     * @throws GridException If injection failed.
     */
    private void inject(GridSpi spi) throws GridException {
        if (spi instanceof GridSpiAdapter) {
            Collection<Object> injectables = ((GridSpiAdapter)spi).injectables();

            if (!F.isEmpty(injectables))
                for (Object o : injectables)
                    ctx.resource().injectGeneric(o);
        }
    }

    /**
     * @param spi SPI whose internal objects need to be injected.
     * @throws GridException If injection failed.
     */
    private void cleanup(GridSpi spi) throws GridException {
        if (spi instanceof GridSpiAdapter) {
            Collection<Object> injectables = ((GridSpiAdapter)spi).injectables();

            if (!F.isEmpty(injectables))
                for (Object o : injectables)
                    ctx.resource().cleanupGeneric(o);
        }
    }

    /**
     * Starts wrapped SPI.
     *
     * @throws GridException If wrapped SPI could not be started.
     */
    protected final void startSpi() throws GridException {
        assert spis != null;
        assert proxies != null;

        Collection<String> names = new HashSet<String>(spis.length);

        for (int i = 0; i < spis.length; i++) {
            GridSpi spi = spis[i];

            // Print-out all SPI parameters only in DEBUG mode.
            if (log.isDebugEnabled())
                log.debug("Starting SPI: " + spi);

            GridSpiInfo info = spi.getClass().getAnnotation(GridSpiInfo.class);

            if (info == null)
                throw new GridException("SPI implementation does not have @GridSpiInfo annotation: " + spi.getClass());

            if (names.contains(spi.getName()))
                throw new GridException("Duplicate SPI name (need to explicitly configure 'setName()' property): " +
                    spi.getName());

            names.add(spi.getName());

            if (log.isDebugEnabled())
                log.debug("Starting SPI implementation: " + spi.getClass().getName());

            try {
                proxies[i].spiStart(ctx.gridName());
            }
            catch (GridSpiException e) {
                throw new GridException("Failed to start SPI: " + spi, e);
            }

            if (log.isDebugEnabled())
                log.debug("SPI module started ok [spi=" + spi.getClass().getName() + ", author=" + info.author() +
                    ", version=" + info.version() + ", email=" + info.email() + ", url=" + info.url() + ']');
        }
    }

    /**
     * Stops wrapped SPI.
     *
     * @throws GridException If underlying SPI could not be stopped.
     */
    protected final void stopSpi() throws GridException {
        for (int i = 0; i < spis.length; i++) {
            GridSpi spi = spis[i];

            if (log.isDebugEnabled())
                log.debug("Stopping SPI: " + spi);

            try {
                proxies[i].spiStop();

                GridSpiInfo info = spi.getClass().getAnnotation(GridSpiInfo.class);

                assert info != null;

                if (log.isDebugEnabled())
                    log.debug("SPI module stopped ok [spi=" + spi.getClass().getName() +
                        ", author=" + info.author() + ", version=" + info.version() +
                        ", email=" + info.email() + ", url=" + info.url() + ']');
            }
            catch (GridSpiException e) {
                throw new GridException("Failed to stop SPI: " + spi, e);
            }

            try {
                cleanup(spi);

                ctx.resource().cleanup(spi);
            }
            catch (GridException e) {
                U.error(log, "Failed to remove injected resources from SPI (ignoring): " + spi, e);
            }
        }
    }

    /**
     * @return Uniformly formatted ack string.
     */
    protected final String startInfo() {
        return "Manager started ok: " + getClass().getName();
    }

    /**
     * @return Uniformly formatted ack string.
     */
    protected final String stopInfo() {
        return "Manager stopped ok: " + getClass().getName();
    }

    /** {@inheritDoc} */
    @Override public final void onKernalStart() throws GridException {
        for (final GridSpi spi : spis) {
            spiRwLock.writeLock().lock();

            try {
                spi.onContextInitialized(new GridSpiContext() {
                    @Override public Collection<GridNode> remoteNodes() {
                        return ctx.discovery().remoteNodes();
                    }

                    @Override public Collection<GridNode> nodes() {
                        return ctx.discovery().allNodes();
                    }

                    @Override public GridNode localNode() {
                        return ctx.discovery().localNode();
                    }

                    @Override public boolean isEnterprise() {
                        return ctx.isEnterprise();
                    }

                    @Nullable @Override public GridNode node(UUID nodeId) {
                        A.notNull(nodeId, "nodeId");

                        return ctx.discovery().node(nodeId);
                    }

                    @Override public boolean pingNode(UUID nodeId) {
                        A.notNull(nodeId, "nodeId");

                        return ctx.discovery().pingNode(nodeId);
                    }

                    @Override public void send(GridNode node, Serializable msg, String topic) throws GridSpiException {
                        A.notNull(node, "node");
                        A.notNull(msg, "msg");
                        A.notNull(topic, "topic");

                        try {
                            ctx.io().send(node, topic, msg, SYSTEM_POOL);
                        }
                        catch (GridException e) {
                            throw unwrapException(e);
                        }
                    }

                    @Override public void send(Collection<? extends GridNode> nodes, Serializable msg,
                        String topic) throws GridSpiException {
                        A.notNull(nodes, "nodes");
                        A.notNull(msg, "msg");
                        A.notNull(topic, "topic");

                        try {
                            ctx.io().send(nodes, topic, msg, PUBLIC_POOL);
                        }
                        catch (GridException e) {
                            throw unwrapException(e);
                        }
                    }

                    @SuppressWarnings("deprecation")
                    @Override public void addMessageListener(GridMessageListener lsnr, String topic) {
                        A.notNull(lsnr, "lsnr");
                        A.notNull(topic, "topic");

                        ctx.io().addMessageListener(topic, lsnr);
                    }

                    @SuppressWarnings("deprecation")
                    @Override public boolean removeMessageListener(GridMessageListener lsnr, String topic) {
                        A.notNull(lsnr, "lsnr");
                        A.notNull(topic, "topic");

                        return ctx.io().removeMessageListener(topic, lsnr);
                    }

                    @Override public void addLocalEventListener(GridLocalEventListener lsnr, int... types) {
                        A.notNull(lsnr, "lsnr");

                        ctx.event().addLocalEventListener(lsnr, types);
                    }

                    @Override public boolean removeLocalEventListener(GridLocalEventListener lsnr) {
                        A.notNull(lsnr, "lsnr");

                        return ctx.event().removeLocalEventListener(lsnr);
                    }

                    @Override public Collection<? extends GridNode> topology(GridTaskSession taskSes,
                        Collection<? extends GridNode> grid) throws GridSpiException {
                        try {
                            return ctx.topology().getTopology((GridTaskSessionInternal)taskSes, grid);
                        }
                        catch (GridException e) {
                            throw unwrapException(e);
                        }
                    }

                    @Override public void recordEvent(GridEvent evt) {
                        A.notNull(evt, "evt");

                        ctx.event().record(evt);
                    }

                    @Override public void registerPort(int port, GridPortProtocol proto) {
                        ctx.ports().registerPort(port, proto, spi.getClass());
                    }

                    @Override public void deregisterPort(int port, GridPortProtocol proto) {
                        ctx.ports().deregisterPort(port, proto, spi.getClass());
                    }

                    @Override public void deregisterPorts() {
                        ctx.ports().deregisterPorts(spi.getClass());
                    }

                    @Nullable @Override public <K, V> V get(String cacheName, K key) throws GridException {
                        return ctx.cache().<K, V>cache(cacheName).get(key);
                    }

                    @Nullable @Override public <K, V> V put(String cacheName, K key, V val, long ttl)
                        throws GridException {
                        GridCacheEntry<K, V> e = ctx.cache().<K, V>cache(cacheName).entry(key);

                        assert e != null;

                        e.timeToLive(ttl);

                        return e.set(val);
                    }

                    @Nullable @Override public <K, V> V putIfAbsent(String cacheName, K key, V val, long ttl)
                        throws GridException {
                        GridCacheEntry<K, V> e = ctx.cache().<K, V>cache(cacheName).entry(key);

                        assert e != null;

                        e.timeToLive(ttl);

                        return e.setIfAbsent(val);
                    }

                    @Nullable @Override public <K, V> V remove(String cacheName, K key) throws GridException {
                        return ctx.cache().<K, V>cache(cacheName).remove(key);
                    }

                    @Override public <K> boolean containsKey(String cacheName, K key) {
                        return ctx.cache().cache(cacheName).containsKey(key);
                    }

                    @Override public void writeToSwap(String spaceName, Object key, @Nullable Object val,
                        @Nullable ClassLoader ldr) throws GridException {
                        ctx.swap().write(spaceName, key, val, ldr);
                    }

                    @SuppressWarnings({"unchecked"})
                    @Nullable @Override public <T> T readFromSwap(String spaceName, Object key,
                        @Nullable ClassLoader ldr) throws GridException {
                        return (T)ctx.swap().read(spaceName, key, ldr);
                    }

                    @Override public void removeFromSwap(String spaceName, Object key,
                        @Nullable ClassLoader ldr) throws GridException {
                        ctx.swap().remove(spaceName, key, null, ldr);
                    }

                    @Override public boolean authenticateNode(UUID nodeId, Map<String, Object> attrs)
                        throws GridException {
                        return ctx.auth().authenticateNode(nodeId, attrs);
                    }

                    /**
                     * @param e Exception to handle.
                     * @return GridSpiException Converted exception.
                     */
                    private GridSpiException unwrapException(GridException e) {
                        // Avoid double-wrapping.
                        if (e.getCause() instanceof GridSpiException)
                            return (GridSpiException)e.getCause();

                        return new GridSpiException("Failed to execute SPI context method.", e);
                    }
                });
            }
            catch (GridSpiException e) {
                throw new GridException("Failed to initialize SPI context.", e);
            }
            finally {
                spiRwLock.writeLock().unlock();
            }
        }

        onKernalStart0();
    }

    /** {@inheritDoc} */
    @Override public final void onKernalStop(boolean cancel, boolean wait) {
        for (GridSpi spi : spis) {
            spiRwLock.writeLock().lock();

            try {
                spi.onContextDestroyed();
            }
            finally {
                spiRwLock.writeLock().unlock();
            }
        }

        onKernalStop0(cancel, wait);
    }

    /**
     * @throws GridException If failed.
     */
    protected void onKernalStart0() throws GridException {
        // No-op.
    }

    /**
     * @param cancel Cancel flag.
     * @param wait Wait flag.
     */
    protected void onKernalStop0(boolean cancel, boolean wait) {
        // No-op.
    }

    /**
     * Throws exception with uniform error message if given parameter's assertion condition
     * is {@code false}.
     *
     * @param cond Assertion condition to check.
     * @param condDesc Description of failed condition. Note that this description should include
     *      JavaBean name of the property (<b>not</b> a variable name) as well condition in
     *      Java syntax like, for example:
     *      <pre name="code" class="java">
     *      ...
     *      assertParameter(dirPath != null, "dirPath != null");
     *      ...
     *      </pre>
     *      Note that in case when variable name is the same as JavaBean property you
     *      can just copy Java condition expression into description as a string.
     * @throws GridException Thrown if given condition is {@code false}
     */
    protected final void assertParameter(boolean cond, String condDesc) throws GridException {
        if (!cond)
            throw new GridException("Grid configuration parameter failed condition check: " + condDesc);
    }

    /** {@inheritDoc} */
    @Override public void printMemoryStats() {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public final String toString() {
        return S.toString(GridManagerAdapter.class, this, "name", getClass().getName());
    }
}
