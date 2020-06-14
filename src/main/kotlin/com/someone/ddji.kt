package com.someone

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import java.io.File

var d = mapOf<Int, String>()
val bd = BilibiliData()

object ddji : PluginBase() {

    override fun onDisable() {
        super.onDisable()
        logger.info(bd.export())
        try {
            bd._videoJob.let { if (!it.isCancelled) it.cancel() }
            bd._liveJob.let { if (!it.isCancelled) it.cancel() }
        }catch (e:Exception){

        }
        val file: File by lazy {
            File("${MiraiConsole.path}/plugins/ddji/output.json")
        }
        if (file.exists()) file.writeText(bd.export())
    }

    override fun onEnable() {
        super.onEnable()
        //bd.runBot()
        logger.info("Plugin loaded!")

        subscribeGroupMessages {
            ".help" reply {
                "欢迎使用dd机！\n" +
                        ".help 入门\n" +
                        ".help 指令\n" +
                        ".help 协议\n" +
                        "本插件具有超级牛力\n" +
                        "power by jvav"
            }
            ".help 入门" reply {
                "发送.bind 名字，有动态和直播的时候都会发消息哒"
            }
            ".help 指令" reply {
                ".bind 名字--绑定b站的人desu\n" +
                        ".uid uid--用uid来绑定desu\n" +
                        ".av av号--用av号来查询视频desu\n" +
                        ".bv bv号--用bv号来查询视频desu\n"+
                        ".rm 名字--删除已经绑定的人desu"
            }
            startsWith(".bv ",removePrefix = true){
                reply(bd.getBv(it))
            }
            startsWith(".av ",removePrefix = true){
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
                bd.run(bot = bot,groupId = group.id)
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
                bd.run(bot = bot,groupId = group.id)
                "在本群启动咯"
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
            Regex(".*")matchingReply {
                if (bd._groups.size<=0) {
                    val file: File by lazy {
                        File("${MiraiConsole.path}/plugins/ddji/output.json")
                    }
                    if (file.exists()) bd.import(file.readText())
                }
                if (bd._groups.any { it==group.id })bd.run(bot,group.id)
            }
        }

        subscribeAlways<MessageRecallEvent> { event ->
            logger.info { "${event.authorId} 的消息被撤回了" }
        }
    }
}