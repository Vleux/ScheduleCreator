package classes.task

import classes.enums.Fairness
import classes.time.Date
import classes.time.Time
import objects.IdKeeper
import objects.Schedule
import objects.Tasks

class Task(
    name: String,
    numberOfPeople: Int,
    dateTime: Map<Date, Array<Pair<Time, Time>>>,
    incompatibleTasks: Array<String> = emptyArray(),
    excludedBy: Array<String> = emptyArray(),
    requiredFairness: Fairness = Fairness.LOW,
    driver: Boolean = false
) : Comparable<Task>{

    // Attributes

    private var _id: String

    private var _name: String = name
    private var _numberOfPeople: Int = numberOfPeople       // The amount of people that are required for this task
    private var _dateTime: Map<Date, Array<Pair<Time, Time>>> = dateTime
    private var _excludesTasks: Array<String> = incompatibleTasks
    private var _excludedBy: Array<String> = excludedBy
    private var _driver: Boolean = driver
    private var _children: Array<String> = arrayOf()
    var requiredFairness: Fairness = requiredFairness
    // Constructors

    // Masks and getters

    var name: String
        get() = this._name
        set(name) {
            if (name.length > 2){
                this.name = name
            }
        }
    var numberOfPeople: Int
        get() = this._numberOfPeople
        set(numb){
            if (numb >= 1){
                this._numberOfPeople = numb
            }
        }
    var dateTime: Map<Date, Array<Pair<Time, Time>>>
        get() = this._dateTime
        set(new){
            if (new.keys.size > 0){
                this._dateTime = new
            }
        }
    val excludesTasks: Array<String>
        get() = this._excludesTasks
    val excludedBy: Array<String>
        get() = this._excludedBy
    var driver: Boolean
        get() = this._driver
        set(new){
            this._driver = driver
        }
    val id: String
        get() = this._id
    val children: Array<String>
        get() = this._children

    /**
     * Adds an task that is excluded by this Task.
     */
    fun addExcludedTask(id: String): Boolean{
        if (Tasks.doesTaskExist(id) && !this.excludesTasks.contains(id) && !this.excludedBy.contains(id)){
            this._excludesTasks += id
            Tasks.getTask(id)!!.addExcludedBy(this.id)
            return true
        }
        return false
    }

    private fun addExcludedBy(id: String): Boolean{
        if (!this.excludesTasks.contains(id) && !this.excludedBy.contains(id)){
            this._excludedBy += id
            return true
        }
        return false
    }

    // overriding some functions

    override fun equals(other: Any?): Boolean {
        return if (other is Task){
            other.id == this.id
        }else{
            false
        }
    }

    override fun compareTo(other: Task): Int{
        if (other.id == this.id){
            return 0
        }

        val otherID = other.id.substring(5).toInt()
        val thisID = this.id.substring(5).toInt()

        return if (otherID > thisID){
            -1
        }else{
            1
        }
    }

    /**
     * Returns all the Time when it should happen on a specified day
     */
    fun getTimeOnDate(date: Date): Array<Pair<Time, Time>>{
        return if (this.dateTime[date] != null) {
            this.dateTime[date]!!
        }
        else{
            emptyArray()
        }
    }

    fun schedule(takenPeople: Array<String> = emptyArray()){
        val sTasks: ArrayList<String> = arrayListOf()
        for (date in this.dateTime){
            for (time in date.value){
                sTasks.add(Schedule.addScheduledTask(
                    date.key,
                    this.id,
                    time,
                    takenPeople,
                ))
            }
        }
        this._children = sTasks.toTypedArray()
    }

    fun isChild(childId: String): Boolean{
        return this.children.contains(childId)
    }

    override fun hashCode(): Int {
        return _id.hashCode()
    }

    init {
        this._id = IdKeeper.getNextTaskId()
    }
}