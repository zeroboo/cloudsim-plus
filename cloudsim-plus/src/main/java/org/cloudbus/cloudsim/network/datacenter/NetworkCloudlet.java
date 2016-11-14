/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

/**
 * NetworkCloudlet class extends Cloudlet to support simulation of complex
 * applications. Each NetworkCloudlet represents a task of the application.
 * Each task consists of several tasks.
 *
 * <p>
 * Please refer to following publication for more details:
 * <ul>
 * <li>
 * <a href="http://dx.doi.org/10.1109/UCC.2011.24">
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel
 * Applications in Cloud Simulations, Proceedings of the 4th IEEE/ACM
 * International Conference on Utility and Cloud Computing (UCC 2011, IEEE CS
 * Press, USA), Melbourne, Australia, December 5-7, 2011.
 * </a>
 * </ul>
 * </p>
 *
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 *
 * @todo @author manoelcampos See how to implement the NULL pattern for this class.
 */
public class NetworkCloudlet extends CloudletSimple {

    /**
     * @see #getCurrentTaskNum()
     */
    private int currentTaskNum;

    /**
     * All tasks which cloudlet execution.
     */
    private final List<CloudletTask> tasks;

    /**
     * @see #getAppCloudlet()
     */
    private AppCloudlet appCloudlet;

    /**
     * @see #getMemory()
     */
    private long memory;

    /**
     * Creates a NetworkCloudlet with no priority and file size and output size equal to 1.
     *
     * @param id the unique ID of this cloudlet
     * @param cloudletLength the length or size (in MI) of this cloudlet to be executed in a VM
     * @param pesNumber the pes number
     * @pre id >= 0
     * @pre cloudletLength >= 0.0
     * @post $none
     */
    public NetworkCloudlet(final int id,  final long cloudletLength,  final int pesNumber) {
        super(id, cloudletLength, pesNumber);
        this.currentTaskNum = -1;
        this.memory = 0;
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a NetworkCloudlet with the given parameters.
     *
     * @param id
     * @param cloudletLength
     * @param pesNumber
     * @param cloudletFileSize
     * @param cloudletOutputSize
     * @param memory
     * @param utilizationModelCpu
     * @param utilizationModelRam
     * @param utilizationModelBw
     *
     * @deprecated Use the other available constructors with less parameters
     * and set the remaining ones using the respective setters.
     * This constructor will be removed in future versions.
     *
     */
    @Deprecated
    private NetworkCloudlet(
            int id,
            final long cloudletLength,
            int pesNumber,
            long cloudletFileSize,
            long cloudletOutputSize,
            long memory,
            UtilizationModel utilizationModelCpu,
            UtilizationModel utilizationModelRam,
            UtilizationModel utilizationModelBw) {
        this(id, cloudletLength, pesNumber);
        this.setCloudletFileSize(cloudletFileSize)
            .setCloudletOutputSize(cloudletOutputSize)
            .setUtilizationModelCpu(utilizationModelCpu)
            .setUtilizationModelRam(utilizationModelRam)
            .setUtilizationModelBw(utilizationModelBw);
        this.memory = memory;
    }

    /**
     * Gets the {@link AppCloudlet} that owns this NetworkCloudlet.
     *
     * @return
     */
    public AppCloudlet getAppCloudlet() {
        return appCloudlet;
    }

    /**
     * Set the {@link AppCloudlet} that owns this NetworkCloudlet.
     *
     * @param appCloudlet
     */
    public NetworkCloudlet setAppCloudlet(AppCloudlet appCloudlet) {
        this.appCloudlet = appCloudlet;
        return this;
    }

    public double getNumberOfTasks() {
        return tasks.size();
    }

    /**
     *
     * @return a read-only list of cloudlet's tasks.
     */
    public List<CloudletTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Gets the Cloudlet's RAM memory.
     *
     * @todo Required, allocated, used memory? It doesn't appear to be used.
     */
    public long getMemory() {
        return memory;
    }

    /**
     * Sets the Cloudlet's RAM memory.
     * @param memory amount of RAM to set
     *
     */
    public NetworkCloudlet setMemory(long memory) {
        this.memory = memory;
        return this;
    }

    /**
     * Gets the index of the current task being executed.
     *
     * @return return the current task number if the Cloudlet started executing,
     * -1 if the Cloudlet hasn't started executing or the number of tasks if all
     * tasks have finished executing.
     */
    public int getCurrentTaskNum() {
        return currentTaskNum;
    }

    /**
     * Indicates if the NetworkCloudlet is executing
     * its last task.
     * @return
     */
    public boolean isTheLastTask(){
        return getCurrentTaskNum() >= tasks.size() - 1;
    }

    /**
     * Change the current task to the next one in order
     * to start executing it, if the current task is finished.
     *
     * @param nextTaskStartTime the time that the next task will start
     * @return the next task or null if there isn't any next one
     */
    public CloudletTask startNextTask(double nextTaskStartTime){
        /**
         * @todo @author manoelcampos CloudletTask should implement
         * Null Object Pattern to avoid these null checks.
         */
        CloudletTask nextTask = getNextTask();
        if(nextTask != null){
            nextTask.setStartTime(nextTaskStartTime);
        }
        return nextTask;
    }

    /**
     * Gets the current task.
     *
     * @return
     */
    protected CloudletTask getCurrentTask() {
        if (currentTaskNum < 0 || currentTaskNum >= tasks.size()) {
            return null;
        }

        return tasks.get(currentTaskNum);
    }

    /**
     * Gets the next task in the list if the current task is finished.
     * @return the next task or null if the current task is already the last one
     * or it is not finished yet.
     */
    protected CloudletTask getNextTask(){
        if(getCurrentTask() != null && !getCurrentTask().isFinished())
            return null;

        if(this.currentTaskNum <= tasks.size()-1)
            this.currentTaskNum++;

        return getCurrentTask();
    }

    @Override
    public boolean isFinished() {
        boolean allTasksFinished = tasks.stream().allMatch(t -> t.isFinished());
        return super.isFinished() && allTasksFinished;
    }

    /**
     * {@inheritDoc}
     * <p>The length of a NetworkCloudlet is the
     * length sum of all its {@link CloudletExecutionTask}'s.</p>
     * @return the length sum of all {@link CloudletExecutionTask}'s
     *
     * @todo @author manoelcampos It is being computed the total
     * length into the getter because the
     * code related to the cloudlet length in classes such as
     * {@link org.cloudbus.cloudsim.schedulers.CloudletSchedulerSpaceShared}
     * is inappropriately changing this property as a mean to
     * control cloudlet states. That issue should be fixed prior to fix this one.
     */
    @Override
    public long getCloudletLength() {
        return getTasks().stream()
                .filter(t -> t instanceof CloudletExecutionTask)
                .mapToLong(t -> ((CloudletExecutionTask)t).getLength())
                .sum();
    }

    @Override
    public boolean setCloudletFinishedSoFar(long length) {
        return super.setCloudletFinishedSoFar(length);
    }

    /**
     * Adds a task to the {@link #getTasks() task list}
     * and links the task to the NetworkCloudlet.
     *
     * @param task Task to be added
     * @return the NetworkCloudlet instance
     */
    public NetworkCloudlet addTask(CloudletTask task) {
        task.setNetworkCloudlet(this);
        tasks.add(task);
        return this;
    }

    protected long numberOfExecutionTasks() {
        return getTasks().stream().filter(t -> t instanceof CloudletExecutionTask).count();
    }

}