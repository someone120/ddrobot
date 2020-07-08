package com.someone

import com.beust.klaxon.Klaxon
import com.google.gson.annotations.SerializedName
import com.someone.BilibiliData.Live
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.message.uploadImage
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.logging.Logger


class BilibiliData {
    data class Stat(
        var name: String,
        var liveStat: Int,
        var lastAid: Int
    )

    private val _liveJob: Job = runLive()
    private val _videoJob: Job = runVideo()
    private var _ids = mutableMapOf<Int, Stat>()
    private var _bot: Bot? = null

    //var Dynamics = mutableMapOf<Long, Long>()
    private var _groups = mutableSetOf<Long>()
    fun set(id: Int, name: String) {
        _ids[id] = Stat(name, 0, 0)
    }

    fun remove(name: String): String {
        val filler = _ids.filter { it.value.name == name }
        return if (filler.isNotEmpty()) {
            remove(filler.entries.toList()[0].key)
        } else {
            "没有这个人吧"
        }
    }

    private fun remove(id: Int): String {
        return if (_ids.any { it.key == id }) {
            val name = _ids[id]?.name
            _ids.remove(id)
            "删除成功！名字为$name"
        } else {
            "没有这个人吧"
        }
    }

    fun export(): String {

        data class DataItem(
            val id: Int = 0,
            val stat: Int = 0,
            val name: String = "",
            val lastAid: Int = 0
        )

        data class Data(var groups: MutableList<Long>, var data: MutableList<DataItem>)

        val ids = Data(_groups.toMutableList(), mutableListOf())
        _ids.entries.forEach { ids.data.add(DataItem(it.key, it.value.liveStat, it.value.name, it.value.lastAid)) }
        return Klaxon().toJsonString(ids)
    }

    fun import(string: String) {
        data class DataItem(
            val id: Int = 0,
            val stat: Int = 0,
            val name: String = "",
            val lastAid: Int = 0
        )

        data class Data(
            var groups: MutableList<Long>,
            var data: MutableList<DataItem>
        )

        val ids = Klaxon().parse<Data>(string)
        _groups = ids?.groups?.toMutableSet()!!
        ids.data.forEach { _ids[it.id] = Stat(it.name, it.stat, lastAid = it.lastAid) }
    }

    fun get(string: String): String? {
        var conn: HttpURLConnection? = null
        var result: String? = null
        try {
            conn = URL(
                string
            ).openConnection() as HttpURLConnection
            conn.connect()
            conn.inputStream.use { input ->
                result = input.bufferedReader().readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }
        return result
    }

    fun getName(mid: Long): String {

        data class Nameplate(
            @SerializedName("condition")
            val condition: String = "",
            @SerializedName("image")
            val image: String = "",
            @SerializedName("image_small")
            val imageSmall: String = "",
            @SerializedName("level")
            val level: String = "",
            @SerializedName("name")
            val name: String = "",
            @SerializedName("nid")
            val nid: Int = 0
        )

        data class Official(
            @SerializedName("desc")
            val desc: String = "",
            @SerializedName("role")
            val role: Int = 0,
            @SerializedName("title")
            val title: String = "",
            @SerializedName("type")
            val type: Int = 0
        )

        data class Pendant(
            @SerializedName("expire")
            val expire: Int = 0,
            @SerializedName("image")
            val image: String = "",
            @SerializedName("image_enhance")
            val imageEnhance: String = "",
            @SerializedName("name")
            val name: String = "",
            @SerializedName("pid")
            val pid: Int = 0
        )

        class SysNotice

        class Theme

        data class Vip(
            @SerializedName("_Status")
            val status: Int = 0,
            @SerializedName("theme_type")
            val themeType: Int = 0,
            @SerializedName("type")
            val type: Int = 0
        )

        data class Data(
            @SerializedName("birthday")
            val birthday: String = "",
            @SerializedName("coins")
            val coins: Int = 0,
            @SerializedName("face")
            val face: String = "",
            @SerializedName("fans_badge")
            val fansBadge: Boolean = false,
            @SerializedName("is_followed")
            val isFollowed: Boolean = false,
            @SerializedName("jointime")
            val jointime: Int = 0,
            @SerializedName("level")
            val level: Int = 0,
            @SerializedName("mid")
            val mid: Int = 0,
            @SerializedName("moral")
            val moral: Int = 0,
            @SerializedName("name")
            val name: String = "",
            @SerializedName("nameplate")
            val nameplate: Nameplate = Nameplate(),
            @SerializedName("official")
            val official: Official = Official(),
            @SerializedName("pendant")
            val pendant: Pendant = Pendant(),
            @SerializedName("rank")
            val rank: Int = 0,
            @SerializedName("sex")
            val sex: String = "",
            @SerializedName("sign")
            val sign: String = "",
            @SerializedName("silence")
            val silence: Int = 0,
            @SerializedName("sys_notice")
            val sysNotice: SysNotice = SysNotice(),
            @SerializedName("theme")
            val theme: Theme = Theme(),
            @SerializedName("top_photo")
            val topPhoto: String = "",
            @SerializedName("vip")
            val vip: Vip = Vip()
        )

        data class Info(
            @SerializedName("code")
            val code: Int = 0,
            @SerializedName("data")
            val `data`: Data = Data(),
            @SerializedName("message")
            val message: String = "",
            @SerializedName("ttl")
            val ttl: Int = 0
        )


        val data = get("https://api.bilibili.com/x/space/acc/info?mid=${mid}&jsonp=jsonp")
        val json = data?.let { Klaxon().parse<Info>(it) }
        return json!!.data.name
    }

    fun bind(string: String): MutableMap<Int, String> {

        data class CostTime(
            @SerializedName("as_request")
            val asRequest: String = "",
            @SerializedName("as_request_format")
            val asRequestFormat: String = "",
            @SerializedName("as_response_format")
            val asResponseFormat: String = "",
            @SerializedName("deserialize_response")
            val deserializeResponse: String = "",
            @SerializedName("get upuser live _Status")
            val getUpuserLiveStatus: String = "",
            @SerializedName("illegal_handler")
            val illegalHandler: String = "",
            @SerializedName("main_handler")
            val mainHandler: String = "",
            @SerializedName("params_check")
            val paramsCheck: String = "",
            @SerializedName("save_cache")
            val saveCache: String = "",
            @SerializedName("total")
            val total: String = ""
        )

        data class ExpList(
            @SerializedName("5510")
            val x5510: Boolean = false
        )


        data class OfficialVerify(
            @SerializedName("desc")
            val desc: String = "",
            @SerializedName("type")
            val type: Int = 0
        )

        data class Re(
            @SerializedName("aid")
            val aid: Int = 0,
            @SerializedName("arcurl")
            val arcurl: String = "",
            @SerializedName("bvid")
            val bvid: String = "",
            @SerializedName("coin")
            val coin: Int = 0,
            @SerializedName("desc")
            val desc: String = "",
            @SerializedName("dm")
            val dm: Int = 0,
            @SerializedName("duration")
            val duration: String = "",
            @SerializedName("fav")
            val fav: Int = 0,
            @SerializedName("is_pay")
            val isPay: Int = 0,
            @SerializedName("is_union_video")
            val isUnionVideo: Int = 0,
            @SerializedName("pic")
            val pic: String = "",
            @SerializedName("play")
            val play: String = "",
            @SerializedName("pubdate")
            val pubdate: Int = 0,
            @SerializedName("title")
            val title: String = ""
        )

        data class Result(
            @SerializedName("fans")
            val fans: Int = 0,
            @SerializedName("gender")
            val gender: Int = 0,
            @SerializedName("hit_columns")
            val hitColumns: List<String> = listOf(),
            @SerializedName("is_live")
            val isLive: Int = 0,
            @SerializedName("is_upuser")
            val isUpuser: Int = 0,
            @SerializedName("level")
            val level: Int = 0,
            @SerializedName("mid")
            val mid: Int = 0,
            @SerializedName("official_verify")
            val officialVerify: OfficialVerify = OfficialVerify(),
            @SerializedName("res")
            val res: List<Re> = listOf(),
            @SerializedName("room_id")
            val roomId: Int = 0,
            @SerializedName("type")
            val type: String = "",
            @SerializedName("uname")
            val uname: String = "",
            @SerializedName("upic")
            val upic: String = "",
            @SerializedName("usign")
            val usign: String = "",
            @SerializedName("verify_info")
            val verifyInfo: String = "",
            @SerializedName("videos")
            val videos: Int = 0
        )

        data class Data(
            @SerializedName("cost_time")
            val costTime: CostTime = CostTime(),
            @SerializedName("egg_hit")
            val eggHit: Int = 0,
            @SerializedName("exp_list")
            val expList: ExpList = ExpList(),
            @SerializedName("numPages")
            val numPages: Int = 0,
            @SerializedName("numResults")
            val numResults: Int = 0,
            @SerializedName("page")
            val page: Int = 0,
            @SerializedName("pagesize")
            val pagesize: Int = 0,
            @SerializedName("result")
            val result: List<Result> = listOf(),
            @SerializedName("rqt_type")
            val rqtType: String = "",
            @SerializedName("seid")
            val seid: String = "",
            @SerializedName("show_column")
            val showColumn: Int = 0,
            @SerializedName("suggest_keyword")
            val suggestKeyword: String = ""
        )

        data class Search(
            @SerializedName("code")
            val code: Int = 0,
            @SerializedName("data")
            val `data`: Data = Data(),
            @SerializedName("message")
            val message: String = "",
            @SerializedName("ttl")
            val ttl: Int = 0
        )


        val result = mutableMapOf<Int, String>()
        val data = get(
            "https://api.bilibili.com/x/web-interface/search/type?context=&search_type=bili_user&page=1&order=&category_id=&user_type=&order_sort=&changing=mid&__refresh__=true&_extra=&highlight=1&single_column=0&keyword=" + URLEncoder.encode(
                string,
                "UTF-8"
            )
        )
        return if (data == null) {
            mutableMapOf()
        } else {
            val json = Klaxon()
                .parse<Search>(data)
            val r = json!!.data.result
            r.forEach {
                result[it.mid] = it.uname
            }
            result
        }
    }

    fun startGroup(bot: Bot, groupId: Long) {
        run(bot)
        _groups.add(groupId)
    }

    fun run(bot: Bot) {
        if (_bot == null) _bot = bot
        //if (_groups.size <= 0) {
        if (!_liveJob.isActive) {
            _liveJob.start()
        }
        if (!_videoJob.isActive) {
            _videoJob.start()
        }
    }

    private fun runLive(): Job {
        return GlobalScope.launch(start = CoroutineStart.LAZY) {
            val file: File by lazy {
                File("${MiraiConsole.path}/plugins/ddji/output.json")
            }
            if (file.exists()) import(file.readText())
            while (true) {
                try {
                    _ids.forEach {
                        delay(1000)
                        val live = getLive(it.key)
                        _groups.forEach { groupId ->
                            if (live.stat == 1) {
                                val group = _bot?.getGroup(groupId)
                                group?.sendMessage(
                                    group.uploadImage(URL(live.cover)).plus(live.message)
                                )
                                file.writeText(export())
                            }
                        }
                    }
                    delay(30 * 1000)
                } catch (e: Exception) {
                    //bot.getFriend(525965357).sendMessage(e.toString())
                    e.printStackTrace()
                }
            }
        }
    }

    private fun runVideo(): Job {
        return GlobalScope.launch(start = CoroutineStart.LAZY) {
            val file: File by lazy {
                File("${MiraiConsole.path}/plugins/ddji/output.json")
            }
            if (file.exists()) import(file.readText())
            while (true) {
                try {
                    _ids.forEach {
                        delay(10_000)
                        val live = getVideo(it.key)
                        _groups.forEach { groupId ->
                            if (live.stat == 1) {
                                val group = _bot?.getGroup(groupId)
                                group?.sendMessage(
                                    live.message
                                )
                                file.writeText(export())
                            }
                        }
                    }
                    delay(1800000)
                } catch (e: Exception) {
                    //bot.getFriend(525965357).sendMessage(e.toString())
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getLive(uid: Int): Live {
        val result = StringBuilder()

        data class Data(
            val broadcast_type: Int,
            val cover: String,
            val liveStatus: Int,
            val online: Int,
            val online_hidden: Int,
            val roomStatus: Int,
            val roomid: Int,
            val roundStatus: Int,
            val title: String,
            val url: String
        )

        data class Live(
            val code: Int,
            val `data`: Data,
            val message: String,
            val ttl: Int
        )

        var d = 0
        val data = get("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=${uid}")
        val json: Live? = Klaxon().parse<Live>(data!!)
        if (json!!.data.liveStatus != _ids[uid]?.liveStat) {
            _ids[uid]?.liveStat = json.data.liveStatus
            if (json.data.liveStatus == 1) {
                result.append("${_ids[uid]?.name}直播啦！直播标题是${json.data.title},传送门->${json.data.url} ")
                d = 1
            }
        }
        Logger.getLogger("ddji").info("获取成功！$data")
        return Live(cover = json.data.cover, message = result.toString(), stat = d)
    }

    private fun getVideo(uid: Int): Video {
        val result = Video(0, "")

        data class Vlist(
            val aid: Int = 0, // 838540890
            val author: String = "", // 近卫局长_blast鱿鱼
            val bvid: String = "", // BV18g4y1q7To
            val comment: Int = 0, // 839
            val copyright: String = "",
            val created: Int = 0, // 1591987907
            val description: String = "", // 本视频是 《高台讲坛》系列的第5期 ，喜欢的可以到我的收藏夹查看喜欢请 关注 点赞三连，即可有后续评测推送，想看什么评测也可私信共有： 莫斯提马 刻俄柏 守林人安比尔 白金评测 黑+普罗旺斯评测本人还有《近卫方舟》系列节目，共10期共有：年+塞雷亚 麦哲伦 星极 赫拉格+夜魔 陈 推进之王 狮蝎 幽灵鲨 煌 评测reference：本视频技能面板来自 prts，网址ak.mooncell.wiki
            val hide_click: Boolean = false, // false
            val is_pay: Int = 0, // 0
            val is_union_video: Int = 0, // 0
            val length: String = "", // 13:40
            val mid: Int = 0, // 4475469
            val pic: String = "", // //i1.hdslb.com/bfs/archive/691713e64431f8d6f13c229238f04ac3293db5b2.jpg
            val play: Int = 0, // 88098
            val review: Int = 0, // 0
            val subtitle: String = "",
            val title: String = "", // 【干员评测 莫斯提马】我 要 打 十 个！！！以柔克刚的干员！【高台讲坛 第四期】【明日方舟】专精3,序时之匙 荒时之锁
            val typeid: Int = 0, // 172
            val video_review: Int = 0 // 1242
        )

        data class Page(
            val count: Int = 0, // 59
            val pn: Int = 0, // 1
            val ps: Int = 0 // 30
        )

        data class MapValue(
            val count: Int = 0, // 1
            val name: String = "", // 动画
            val tid: Int = 0 // 1
        )

        data class ListX(
            val tlist: Map<Int, MapValue>? = mapOf(),
            val vlist: List<Vlist> = listOf(Vlist(0))
        )

        data class Data(
            val list: ListX = ListX(),
            val page: Page = Page()
        )

        data class Video(
            val code: Int = 0, // 0
            val `data`: Data = Data(),
            val message: String = "", // 0
            val ttl: Int = 0 // 1
        )

        val json =
            get("https://api.bilibili.com/x/space/arc/search?mid=${uid}&ps=1&tid=0&pn=1&keyword=&order=pubdate")?.let {
                var resultJson: Video? = Video()
                try {
                    resultJson = Klaxon().parse<Video>(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                resultJson
            }
        json?.let { video ->
            if (video.data.page.count > 0) {
                video.data.list.let {
                    if (_ids[uid]?.lastAid != it.vlist[0].aid) {
                        _ids[uid]?.lastAid = it.vlist[0].aid
                        result.stat = 1
                        //result.cover = json.data.list.vlist[0].pic
                        result.message =
                            "${it.vlist[0].author}发布了新视频！\n" +
                                    "视频名字为：${it.vlist[0].title}\n" +
                                    "传送门-> https://www.bilibili.com/video/${it.vlist[0].bvid}"
                    }
                }
            }
        }
        return result
    }

    fun getAv(Aid: Int): String {
        val data = get("https://api.bilibili.com/x/web-interface/view?aid=$Aid")

        data class Dimension(
            val height: Int = 0, // 0
            val rotate: Int = 0, // 0
            val width: Int = 0 // 0
        )

        data class Owner(
            val face: String = "", // http://i2.hdslb.com/bfs/face/40c46ee74dd6ea33d46c38cd6083e6a1286aa482.gif
            val mid: Int = 0, // 122541
            val name: String = "" // 冰封.虾子
        )

        data class Rights(
            val autoplay: Int = 0, // 1
            val bp: Int = 0, // 0
            val download: Int = 0, // 1
            val elec: Int = 0, // 0
            val hd5: Int = 0, // 0
            val is_cooperation: Int = 0, // 0
            val movie: Int = 0, // 0
            val no_background: Int = 0, // 0
            val no_reprint: Int = 0, // 0
            val pay: Int = 0, // 0
            val ugc_pay: Int = 0, // 0
            val ugc_pay_preview: Int = 0 // 0
        )

        data class Stat(
            val aid: Int = 0, // 170001
            val coin: Int = 0, // 188834
            val danmaku: Int = 0, // 832488
            val dislike: Int = 0, // 0
            val evaluation: String = "",
            val favorite: Int = 0, // 694730
            val his_rank: Int = 0, // 13
            val like: Int = 0, // 457333
            val now_rank: Int = 0, // 0
            val reply: Int = 0, // 132708
            val share: Int = 0, // 509038
            val view: Int = 0 // 27685357
        )

        data class Subtitle(
            val allow_submit: Boolean = false, // false
            val list: List<Any> = listOf()
        )

        data class DimensionX(
            val height: Int = 0, // 0
            val rotate: Int = 0, // 0
            val width: Int = 0 // 0
        )


        data class Page(
            val cid: Int = 0, // 279786
            val dimension: DimensionX = DimensionX(),
            val duration: Int = 0, // 199
            val from: String = "", // vupload
            val page: Int = 0, // 1
            val part: String = "", // Хоп
            val vid: String = "",
            val weblink: String = ""
        )

        data class Data(
            val aid: Int = 0, // 170001
            val attribute: Int = 0, // 2130003
            val bvid: String = "", // BV17x411w7KC
            val cid: Int = 0, // 279786
            val copyright: Int = 0, // 2
            val ctime: Int = 0, // 1497380562
            val desc: String = "", // sina 保加利亚超级天王 Azis1999年出道。他的音乐融合保加利亚名族曲风chalga和pop、rap等元素，不过他惊艳的易装秀与浮夸的角色诠释才是他最为出名的地方 Azis与众多保加利亚天王天后级歌手都有过合作.06年，他作为Mariana Popova的伴唱，在欧洲半决赛上演唱了他们的参赛曲Let Me Cry 06年他被Velikite Balgari评为保加利亚有史以来最伟大的名人之一
            val dimension: Dimension = Dimension(),
            val duration: Int = 0, // 2412
            val `dynamic`: String = "",
            val no_cache: Boolean = false, // false
            val owner: Owner = Owner(),
            val pages: List<Page> = listOf(),
            val pic: String = "", // http://i2.hdslb.com/bfs/archive/1ada8c32a9d168e4b2ee3e010f24789ba3353785.jpg
            val pubdate: Int = 0, // 1320850533
            val rights: Rights = Rights(),
            val stat: Stat = Stat(),
            val state: Int = 0, // 0
            val subtitle: Subtitle = Subtitle(),
            val tid: Int = 0, // 193
            val title: String = "", // 【MV】保加利亚妖王AZIS视频合辑
            val tname: String = "", // MV
            val videos: Int = 0 // 10
        )

        data class Video(
            val code: Int = 0, // 0
            val `data`: Data = Data(),
            val message: String = "", // 0
            val ttl: Int = 0 // 1
        )

        val json = Klaxon().parse<Video>(data!!)
        return "标题：${json!!.data.title}AV号：${json.data.aid}\nBV号：${json.data.bvid}\n播放数：${json.data.stat.view}\n" +
                "硬币数：${json.data.stat.coin}\n收藏数：${json.data.stat.favorite}"
    }

    fun getBv(Bvid: String): String {
        val data = get("https://api.bilibili.com/x/web-interface/view?bvid=$Bvid")

        data class Dimension(
            val height: Int = 0, // 0
            val rotate: Int = 0, // 0
            val width: Int = 0 // 0
        )

        data class Owner(
            val face: String = "", // http://i2.hdslb.com/bfs/face/40c46ee74dd6ea33d46c38cd6083e6a1286aa482.gif
            val mid: Int = 0, // 122541
            val name: String = "" // 冰封.虾子
        )

        data class Rights(
            val autoplay: Int = 0, // 1
            val bp: Int = 0, // 0
            val download: Int = 0, // 1
            val elec: Int = 0, // 0
            val hd5: Int = 0, // 0
            val is_cooperation: Int = 0, // 0
            val movie: Int = 0, // 0
            val no_background: Int = 0, // 0
            val no_reprint: Int = 0, // 0
            val pay: Int = 0, // 0
            val ugc_pay: Int = 0, // 0
            val ugc_pay_preview: Int = 0 // 0
        )

        data class Stat(
            val aid: Int = 0, // 170001
            val coin: Int = 0, // 188834
            val danmaku: Int = 0, // 832488
            val dislike: Int = 0, // 0
            val evaluation: String = "",
            val favorite: Int = 0, // 694730
            val his_rank: Int = 0, // 13
            val like: Int = 0, // 457333
            val now_rank: Int = 0, // 0
            val reply: Int = 0, // 132708
            val share: Int = 0, // 509038
            val view: Int = 0 // 27685357
        )

        data class Subtitle(
            val allow_submit: Boolean = false, // false
            val list: List<Any> = listOf()
        )

        data class DimensionX(
            val height: Int = 0, // 0
            val rotate: Int = 0, // 0
            val width: Int = 0 // 0
        )


        data class Page(
            val cid: Int = 0, // 279786
            val dimension: DimensionX = DimensionX(),
            val duration: Int = 0, // 199
            val from: String = "", // vupload
            val page: Int = 0, // 1
            val part: String = "", // Хоп
            val vid: String = "",
            val weblink: String = ""
        )

        data class Data(
            val aid: Int = 0, // 170001
            val attribute: Int = 0, // 2130003
            val bvid: String = "", // BV17x411w7KC
            val cid: Int = 0, // 279786
            val copyright: Int = 0, // 2
            val ctime: Int = 0, // 1497380562
            val desc: String = "", // sina 保加利亚超级天王 Azis1999年出道。他的音乐融合保加利亚名族曲风chalga和pop、rap等元素，不过他惊艳的易装秀与浮夸的角色诠释才是他最为出名的地方 Azis与众多保加利亚天王天后级歌手都有过合作.06年，他作为Mariana Popova的伴唱，在欧洲半决赛上演唱了他们的参赛曲Let Me Cry 06年他被Velikite Balgari评为保加利亚有史以来最伟大的名人之一
            val dimension: Dimension = Dimension(),
            val duration: Int = 0, // 2412
            val `dynamic`: String = "",
            val no_cache: Boolean = false, // false
            val owner: Owner = Owner(),
            val pages: List<Page> = listOf(),
            val pic: String = "", // http://i2.hdslb.com/bfs/archive/1ada8c32a9d168e4b2ee3e010f24789ba3353785.jpg
            val pubdate: Int = 0, // 1320850533
            val rights: Rights = Rights(),
            val stat: Stat = Stat(),
            val state: Int = 0, // 0
            val subtitle: Subtitle = Subtitle(),
            val tid: Int = 0, // 193
            val title: String = "", // 【MV】保加利亚妖王AZIS视频合辑
            val tname: String = "", // MV
            val videos: Int = 0 // 10
        )

        data class Video(
            val code: Int = 0, // 0
            val `data`: Data = Data(),
            val message: String = "", // 0
            val ttl: Int = 0 // 1
        )

        val json = Klaxon().parse<Video>(data!!)
        return "标题：${json!!.data.title}AV号：${json.data.aid}\nBV号：${json.data.bvid}\n播放数：${json.data.stat.view}\n" +
                "硬币数：${json.data.stat.coin}\n收藏数：${json.data.stat.favorite}"

    }

    fun stopGroup(group: Long): Boolean {
        return _groups.remove(group)
    }

    fun getGroup(): MutableSet<Long>{
        return _groups
    }

    fun stopJob(){
        if (_liveJob.isActive) {
            _liveJob.cancel()
        }
        if (_videoJob.isActive){
            _videoJob.cancel()
        }
    }

    data class Live(
        val stat: Int,
        val cover: String,
        val message: String
    )

    data class Video(
        var stat: Int,
        // var cover: String,
        var message: String
    )

}