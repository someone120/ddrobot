package com.someone

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.utils.info
import java.io.File

var d = mapOf<Int, String>()
val bd = BilibiliData()

object ddji : PluginBase() {

    override fun onDisable() {
        super.onDisable()
        logger.info(bd.export())
        val file: File by lazy {
            File("${MiraiConsole.path}/plugins/ddji/output.json")
        }
        if (file.exists()) file.writeText(bd.export())
        bd.stopJob()
    }

    override fun onEnable() {
        super.onEnable()
        //bd.runBot()
        logger.info("Plugin loaded!")

        subscribeGroupMessages {
            ".help" reply {
                "欢迎使用dd机！\n" +
                        "本插件在检测到有发新视频和直播的时候都会发消息提示\n" +
                        ".help 入门\n" +
                        ".help 指令\n" +
                        ".help 协议\n" +
                        "本插件具有超级牛力\n" +
                        "power by jvav"
            }
            ".help 入门" reply {
                "发送.bind 名字，有发新视频和直播的时候都会发消息哒"
            }
            ".help 指令" reply {
                ".bind 名字--绑定b站的up主desu\n" +
                        ".uid uid--用uid来绑定desu\n" +
                        ".av av号--用av号来查询视频desu\n" +
                        ".bv bv号--用bv号来查询视频desu\n" +
                        ".rm 名字--删除已经绑定的up主desu"
            }
            startsWith(".bv ", removePrefix = true) {
                reply(bd.getBv(it))
            }
            startsWith(".av ", removePrefix = true) {
                reply(bd.getAv(it.toInt()))
            }
            Regex("\\.rm .+") matchingReply {
                bd.remove(it.value.drop(4))
            }
            startsWith(".bind ", removePrefix = true) {
                if (d.isNotEmpty()) {
                    d = mapOf()
                }
                d = bd.bind(it)
                if (d.size > 1) {
                    val message = StringBuilder("好像搜到了多个人哦，找到要d的人之后发送‘.b 前面的序号’吧：")
                    d.forEach { (t, u) -> message.append("\n${t}---${u}") }
                    reply(message.toString())
                } else {
                    bd.set(d.keys.toList()[0], d.values.toList()[0])
                    reply("添加成功！")
                }
                bd.startGroup(bot = bot, groupId = group.id)
            }
            ".导出" reply {
                bd.export()
            }
            startsWith(".导入 ", removePrefix = true) {
                bd.import(it)
                reply("成功！")
            }
            startsWith(".uid ", removePrefix = true) {
                val uid = it
                val name = bd.getName(uid.toLong())
                bd.set(uid.toInt(), name)
                reply("添加成功！名字为$name")
            }
            ".run" reply {
                bd.startGroup(bot = bot, groupId = group.id)
                "在本群启动咯"
            }
            ".stop" reply {
                bd.stopGroup(group.id)
                "关闭了哦"
            }
            startsWith(".check ", removePrefix = true) {
                val instruction = it.split(" ")
                when (instruction[0]) {
                    "group" -> {
                        reply("本群启动状态：${bd.getGroup().any { it == group.id }}")
                    }
                    "bot" -> {
                        reply("Pong!")
                    }
                    "memory"->{
                        reply("剩余内存 = ${(Runtime.getRuntime().freeMemory()).toDouble()/1024/1024}M/${Runtime.getRuntime().maxMemory().toDouble()/1024/1024}M")
                    }
                    "gc"->{
                        Runtime.getRuntime().gc()
                    }
                }
            }
            Regex("\\.b \\d+") matchingReply { result ->
                val ms = result.value.drop(3)
                when {
                    d.isEmpty() -> {
                        "没有搜索吧"
                    }
                    d.any { it.key == ms.toInt() } -> {
                        bd.set(ms.toInt(), d[ms.toInt()] ?: error(""))
                        "添加成功！名字为${d[ms.toInt()]}"
                    }
                    else -> {
                        "没有这个序号吧"
                    }
                }
            }
        }
        subscribeOnce<BotOnlineEvent> {
            bd.run(bot = bot)
        }
    }
}