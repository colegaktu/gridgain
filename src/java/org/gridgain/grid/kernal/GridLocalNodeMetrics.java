// Copyright (C) GridGain Systems, Inc. Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal;

import org.gridgain.grid.*;
import org.gridgain.grid.typedef.internal.*;

/**
 * Local node metrics MBean.
 *
 * @author 2005-2011 Copyright (C) GridGain Systems, Inc.
 * @version 3.5.0c.28102011
 */
public class GridLocalNodeMetrics implements GridNodeMetricsMBean {
    /** Grid node. */
    private final GridNode node;

    /**
     * @param node Node to manage.
     */
    public GridLocalNodeMetrics(GridNode node) {
        assert node != null;

        this.node = node;
    }

    /** {@inheritDoc} */
    @Override public int getTotalCpus() {
        return node.metrics().getTotalCpus();
    }

    /** {@inheritDoc} */
    @Override public float getAverageActiveJobs() {
        return node.metrics().getAverageActiveJobs();
    }

    /** {@inheritDoc} */
    @Override public float getAverageCancelledJobs() {
        return node.metrics().getAverageCancelledJobs();
    }

    /** {@inheritDoc} */
    @Override public double getAverageJobExecuteTime() {
        return node.metrics().getAverageJobExecuteTime();
    }

    /** {@inheritDoc} */
    @Override public double getAverageJobWaitTime() {
        return node.metrics().getAverageJobWaitTime();
    }

    /** {@inheritDoc} */
    @Override public float getAverageRejectedJobs() {
        return node.metrics().getAverageRejectedJobs();
    }

    /** {@inheritDoc} */
    @Override public float getAverageWaitingJobs() {
        return node.metrics().getAverageWaitingJobs();
    }

    /** {@inheritDoc} */
    @Override public float getBusyTimePercentage() {
        return node.metrics().getBusyTimePercentage() * 100;
    }

    /** {@inheritDoc} */
    @Override public int getCurrentActiveJobs() {
        return node.metrics().getCurrentActiveJobs();
    }

    /** {@inheritDoc} */
    @Override public int getCurrentCancelledJobs() {
        return node.metrics().getCurrentCancelledJobs();
    }

    /** {@inheritDoc} */
    @Override public long getCurrentIdleTime() {
        return node.metrics().getCurrentIdleTime();
    }

    /** {@inheritDoc} */
    @Override public long getCurrentJobExecuteTime() {
        return node.metrics().getCurrentJobExecuteTime();
    }

    /** {@inheritDoc} */
    @Override public long getCurrentJobWaitTime() {
        return node.metrics().getCurrentJobWaitTime();
    }

    /** {@inheritDoc} */
    @Override public int getCurrentRejectedJobs() {
        return node.metrics().getCurrentRejectedJobs();
    }

    /** {@inheritDoc} */
    @Override public int getCurrentWaitingJobs() {
        return node.metrics().getCurrentWaitingJobs();
    }

    /** {@inheritDoc} */
    @Override public int getCurrentDaemonThreadCount() {
        return node.metrics().getCurrentDaemonThreadCount();
    }

    /** {@inheritDoc} */
    @Override public long getFileSystemFreeSpace() {
        return node.metrics().getFileSystemFreeSpace();
    }

    /** {@inheritDoc} */
    @Override public long getFileSystemTotalSpace() {
        return node.metrics().getFileSystemTotalSpace();
    }

    /** {@inheritDoc} */
    @Override public long getFileSystemUsableSpace() {
        return node.metrics().getFileSystemUsableSpace();
    }

    /** {@inheritDoc} */
    @Override public long getHeapMemoryCommitted() {
        return node.metrics().getHeapMemoryCommitted();
    }

    /** {@inheritDoc} */
    @Override public long getHeapMemoryInitialized() {
        return node.metrics().getHeapMemoryInitialized();
    }

    /** {@inheritDoc} */
    @Override public long getHeapMemoryMaximum() {
        return node.metrics().getHeapMemoryMaximum();
    }

    /** {@inheritDoc} */
    @Override public long getHeapMemoryUsed() {
        return node.metrics().getHeapMemoryUsed();
    }

    /** {@inheritDoc} */
    @Override public float getIdleTimePercentage() {
        return node.metrics().getIdleTimePercentage() * 100;
    }

    /** {@inheritDoc} */
    @Override public long getLastUpdateTime() {
        return node.metrics().getLastUpdateTime();
    }

    /** {@inheritDoc} */
    @Override public int getMaximumActiveJobs() {
        return node.metrics().getMaximumActiveJobs();
    }

    /** {@inheritDoc} */
    @Override public int getMaximumCancelledJobs() {
        return node.metrics().getMaximumCancelledJobs();
    }

    /** {@inheritDoc} */
    @Override public long getMaximumJobExecuteTime() {
        return node.metrics().getMaximumJobExecuteTime();
    }

    /** {@inheritDoc} */
    @Override public long getMaximumJobWaitTime() {
        return node.metrics().getMaximumJobWaitTime();
    }

    /** {@inheritDoc} */
    @Override public int getMaximumRejectedJobs() {
        return node.metrics().getMaximumRejectedJobs();
    }

    /** {@inheritDoc} */
    @Override public int getMaximumWaitingJobs() {
        return node.metrics().getMaximumWaitingJobs();
    }

    /** {@inheritDoc} */
    @Override public long getNonHeapMemoryCommitted() {
        return node.metrics().getNonHeapMemoryCommitted();
    }

    /** {@inheritDoc} */
    @Override public long getNonHeapMemoryInitialized() {
        return node.metrics().getNonHeapMemoryInitialized();
    }

    /** {@inheritDoc} */
    @Override public long getNonHeapMemoryMaximum() {
        return node.metrics().getNonHeapMemoryMaximum();
    }

    /** {@inheritDoc} */
    @Override public long getNonHeapMemoryUsed() {
        return node.metrics().getNonHeapMemoryUsed();
    }

    /** {@inheritDoc} */
    @Override public int getMaximumThreadCount() {
        return node.metrics().getMaximumThreadCount();
    }

    /** {@inheritDoc} */
    @Override public long getStartTime() {
        return node.metrics().getStartTime();
    }

    /** {@inheritDoc} */
    @Override public long getNodeStartTime() {
        return node.metrics().getNodeStartTime();
    }

    /** {@inheritDoc} */
    @Override public double getCurrentCpuLoad() {
        return node.metrics().getCurrentCpuLoad() * 100;
    }

    /** {@inheritDoc} */
    @Override public double getAverageCpuLoad() {
        return node.metrics().getAverageCpuLoad() * 100;
    }

    /** {@inheritDoc} */
    @Override public int getCurrentThreadCount() {
        return node.metrics().getCurrentThreadCount();
    }

    /** {@inheritDoc} */
    @Override public long getTotalBusyTime() {
        return node.metrics().getTotalBusyTime();
    }

    /** {@inheritDoc} */
    @Override public int getTotalCancelledJobs() {
        return node.metrics().getTotalCancelledJobs();
    }

    /** {@inheritDoc} */
    @Override public int getTotalExecutedJobs() {
        return node.metrics().getTotalExecutedJobs();
    }

    /** {@inheritDoc} */
    @Override public long getTotalIdleTime() {
        return node.metrics().getTotalIdleTime();
    }

    /** {@inheritDoc} */
    @Override public int getTotalRejectedJobs() {
        return node.metrics().getTotalRejectedJobs();
    }

    /** {@inheritDoc} */
    @Override public long getTotalStartedThreadCount() {
        return node.metrics().getTotalStartedThreadCount();
    }

    /** {@inheritDoc} */
    @Override public long getUpTime() {
        return node.metrics().getUpTime();
    }

    /** {@inheritDoc} */
    @Override public long getLastDataVersion() {
        return node.metrics().getLastDataVersion();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridLocalNodeMetrics.class, this);
    }
}
