package classes.generate

import classes.data.Fairness
import classes.data.Limit
import classes.data.PersonLists
import classes.time.Date
import classes.time.Time
import classes.time.WorkDays
import objects.People
import objects.Tasks
import kotlin.properties.Delegates

class Generator {
    private lateinit var personLists: PersonLists
    private lateinit var limits: Limit
    private var maxTime by Delegates.notNull<Int>()
    fun start(){
        this.calculateLimits()
        this.personLists = PersonLists()
    }

    /**
     * Calculates the Limits while ensuring that the Limits are not too low
     */
    private fun calculateLimits(){
        // Calculate the Amount of Tasks (appearance of a Task * needed People)
        var allTaskCount = 0
        var maxFairnessTaskCount = 0
        var medFairnessTaskCount = 0
        var lowFairnessTaskCount = 0
        val tasks = Tasks.getAllTasks()

        for (task in tasks){
            var count = 0

            val dateTime = task.dateTime
            for (key in dateTime.keys){
                count += dateTime[key]!!.size
            }

            count *= task.numberOfPeople
            allTaskCount += count

            when (task.requiredFairness){
                Fairness.LOW -> lowFairnessTaskCount += count
                Fairness.MEDIUM -> medFairnessTaskCount += count
                Fairness.MAXIMUM -> maxFairnessTaskCount += count
            }
        }

        // Security check
        if ((maxFairnessTaskCount + medFairnessTaskCount + lowFairnessTaskCount) != allTaskCount){
            println("VITAL PROBLEM IN CALCULATING TASKS. PLEASE FIX.")
            println("PROGRAM WILL BE HALTED IMMEDIATELY")
            throw Error("FATAL")
        }

        // Calculate the Limits (task / amountOfPeople)

        val amountOfPeople = this.getAmountOfPeople()
        var genLimit = allTaskCount / amountOfPeople
        var maxFairLimit = maxFairnessTaskCount / amountOfPeople
        var medFairLimit = medFairnessTaskCount / amountOfPeople

        // Checks that the Limit is not too small - if it is it will be increased
        while (genLimit * amountOfPeople < allTaskCount){genLimit++}
        while (maxFairLimit * amountOfPeople < maxFairnessTaskCount){maxFairLimit++}
        while (medFairLimit * amountOfPeople < medFairnessTaskCount){medFairLimit++}

        // Save the Limits
        this.limits = Limit(
            genLimit,
            maxFairLimit,
            medFairLimit
        )


    }

    /**
     * Calculates the working-time people (whole time = 1, half-time = 0.5 ...)
     * Adds it up in the end and rounds down (aka casts to an INT)
     */
    private fun getAmountOfPeople(): Int{
        // Get the first arrival and the last leave of a person (the "maxDays"
        // Could also be done with the tasks - but people are more important!

        val ids = People.getAllPeopleIDs()
        var firstDay: Date = People.getPersonById(ids.first())!!.visit.getFirstDay()
        var lastDay: Date = People.getPersonById(ids.first())!!.visit.getLastDay()

        for (id in ids){
            if (People.getPersonById(id)!!.visit.getFirstDay() < firstDay){
                firstDay = People.getPersonById(id)!!.visit.getFirstDay()
            }
            if (People.getPersonById(id)!!.visit.getLastDay() > lastDay){
                lastDay = People.getPersonById(id)!!.visit.getLastDay()
            }
        }
        this.maxTime = WorkDays(firstDay, Time("00:00"), lastDay, Time("00:00")).getWorkDays().size - 1

        /*
        Calculates the available Work time (the amount of people).
        If somebody is only there for 5 out of 10 days, he will be counted as 0.5 Persons in order to decrease the
        amount of tasks he has to fulfill.
         */
        var people = 0.0

        for (id in ids){
            val person = People.getPersonById(id)!!
            var amountOfDays = person.visit.getWorkDays().size.toDouble()
            if (person.visit.timeOfArrival > Time("12:00")){
                amountOfDays -= 0.5
            }
            if (person.visit.timeOfLeave < Time("14:00")){
                amountOfDays -= 0.5
            }
            person.timePercentage = maxTime / amountOfDays
            people += person.timePercentage
        }

        return people.toInt()
    }


}