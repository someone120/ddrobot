package com.someone

import com.beust.klaxon.Klaxon
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.info
import com.google.gson.annotations.SerializedName


var d = mapOf<Long, String>()
val bd = BilibiliData()

object ddji : PluginBase() {
    override fun onLoad() {
        super.onLoad()
    }

    override fun onDisable() {
        super.onDisable()
        logger.info(bd.export())
    }
    override fun onEnable() {
        super.onEnable()

        logger.info("Plugin loaded!")

        subscribeGroupMessages {
            ".help" reply {
                "欢迎使用dd机！\n" +
                        ".help 入门\n" +
                        ".help 指令\n" +
                        ".help 协议"
            }
            ".help 入门" reply {
                "发送.band 名字，有动态和直播的时候都会发消息哒"
            }
            ".help 指令" reply {
                ".band 名字--绑定b站的人desu\n" +
                        ".uid uid--用uid来绑定desu\n" +
                        ".av av号--用av号来查询视频desu\n" +
                        ".bv bv号--用bv号来查询视频desu"
            }
            startsWith(".band ", removePrefix = true) {
                if (d.size != 0) {
                    d = mapOf()
                }
                d = bd.band(it)
                if (d.size > 1) {
                    val message = StringBuilder("好像搜到了多个人哦，找到要d的人之后发送‘.b 前面的序号’吧：")
                    d.forEach { t, u -> message.append("\n${t}---${u}") }
                    reply(message.toString())
                } else {
                    bd.uid(d.keys.toList()[0], d.values.toList()[0])
                    reply("添加成功！")
                }
                bd.run(bot, group)
            }
            ".导出" reply {
                bd.export()
            }
            startsWith(".导入 ",removePrefix = true){
                bd.import(it)
                reply("成功！")
            }
            startsWith(".uid ",removePrefix = true) {
                val uid = it
                val name=bd.getName(uid.toLong())
                bd.uid(uid.toLong(),name)
                reply("添加成功！名字为$name")
            }
            ".run" reply{
                bd.run(bot, group)
                "在本群启动咯"
            }
            Regex(".b .*") matchingReply {
                val ms = it.value.drop(3)
                if (d.size == 0) {
                    "没有搜索吧"
                } else if (d.any { it.key == ms.toLong() }) {
                    bd.uid(ms.toLong(), d.get(ms.toLong())!!)
                    "添加成功！名字为${d.get(ms.toLong())}"
                } else {

                    "没有这个序号吧"
                }
            }
        }

        subscribeAlways<MessageRecallEvent> { event ->
            logger.info { "${event.authorId} 的消息被撤回了" }
        }
    }
}