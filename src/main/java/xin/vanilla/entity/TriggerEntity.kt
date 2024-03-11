package xin.vanilla.entity

import org.quartz.Trigger
import java.util.*

class TriggerEntity {

    var trigger: Trigger? = null

    /**
     * 最近执行时间
     */
    var runTime: List<Date> = mutableListOf()

}
