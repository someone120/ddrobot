package com.someone

import com.beust.klaxon.Klaxon
import com.google.gson.annotations.SerializedName
import com.someone.BilibiliData.Live
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.uploadImage
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.logging.Logger


class BilibiliData {
    private var _ids = mutableMapOf<Long, String>()

    //var Dynamics = mutableMapOf<Long, Long>()
    private var _status = mutableMapOf<Long, Int>()
    private var _bot: Bot? = null
    private val _groups = mutableSetOf<Group>()
    fun uid(id: Long, name: String) {
        _ids[id] = name
        _status[id] = 0
    }

    fun export(): String {
        data class Id(
            @SerializedName("id")
            val id: Long = 0,
            @SerializedName("name")
            val name: String = ""
        )

        data class Status(
            @SerializedName("id")
            val id: Long = 0,
            @SerializedName("_Status")
            val status: Int = 0
        )

        data class Data(
            @SerializedName("_Ids")
            var ids: List<Id> = mutableListOf(),
            @SerializedName("_Status")
            var status: List<Status> = mutableListOf()
        )

        val d = Data()
        this._ids.forEach { (t, u) -> d.ids += Id(id = t, name = u) }
        this._status.forEach { (t, u) -> d.status += Status(id = t, status = u) }
        return Klaxon().toJsonString(d)
    }

    fun import(string: String) {
        data class Id(
            @SerializedName("id")
            val id: Long = 0,
            @SerializedName("name")
            val name: String = ""
        )

        data class Status(
            @SerializedName("id")
            val id: Long = 0,
            @SerializedName("_Status")
            val status: Int = 0
        )

        data class Data(
            @SerializedName("_Ids")
            val ids: List<Id> = listOf(),
            @SerializedName("_Status")
            val status: List<Status> = listOf()
        )

        val d = Klaxon().parse<Data>(string)
        d!!.ids.forEach { this._ids[it.id] = it.name }
        d.status.forEach { this._status[it.id] = it.status }
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

    fun band(string: String): Map<Long, String> {

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


        val result = mutableMapOf<Long, String>()
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
                result[it.mid.toLong()] = it.uname
            }
            result
        }
    }

    fun run(bot: Bot, group: Group) {
        if (this._bot == null) {
            this._bot = bot
        }
        _groups.add(group)
        GlobalScope.launch {
            while (true) {
                try {
                    delay(10 * 1000)
                    _ids.forEach {
                        delay(1000)
                        _groups.forEach { group ->
                            val live = getLive(it.key)
                            if (live.stat == 1) group.sendMessage(group.uploadImage(URL(live.cover)).plus(live.message))
                        }
                    }
                } catch (e: Exception) {
                    bot.getFriend(525965357).sendMessage(e.toString())
                }
            }
        }
    }

    private fun getLive(uid: Long): Live {
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
        if (json!!.data.liveStatus != _status[uid]) {
            _status[uid] = json.data.liveStatus
            if (json.data.liveStatus == 1) {
                result.append("${_ids[uid]}直播啦！直播标题是${json.data.title},快到${json.data.url} 看8")
                d = 1
            }
        }
        Logger.getLogger("ddji").info("获取成功！$data")
        return Live(cover = json.data.cover, message = result.toString(), stat = d)
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
        val data = get("https://api.bilibili.com/x/web-interface/view?aid=$Bvid")

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

    data class Live(
        val stat: Int,
        val cover: String,
        val message: String
    )
}