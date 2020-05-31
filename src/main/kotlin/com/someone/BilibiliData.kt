package com.someone

import com.beust.klaxon.Klaxon
import com.google.gson.annotations.SerializedName
import com.someone.BilibiliData.live
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
    var ids = mutableMapOf<Long, String>()
    var dynamics = mutableMapOf<Long, Long>()
    var status = mutableMapOf<Long, Int>()
    var bot: Bot? = null
    val groups = mutableSetOf<Group>()
    fun uid(id: Long, name: String) {
        ids.put(id, name)
        status.put(id, 0)
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
            @SerializedName("status")
            val status: Int = 0
        )

        data class data(
            @SerializedName("ids")
            var ids: List<Id> = mutableListOf(),
            @SerializedName("status")
            var status: List<Status> = mutableListOf()
        )

        val d = data()
        this.ids.forEach { t, u -> d.ids += Id(id = t, name = u) }
        this.status.forEach { t, u -> d.status += Status(id = t, status = u) }
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
            @SerializedName("status")
            val status: Int = 0
        )

        data class data(
            @SerializedName("ids")
            val ids: List<Id> = listOf(),
            @SerializedName("status")
            val status: List<Status> = listOf()
        )

        val d = Klaxon().parse<data>(string)
        d!!.ids.forEach { this.ids.put(it.id, it.name) }
        d.status.forEach { this.status.put(it.id, it.status) }
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
            @SerializedName("status")
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

        data class info(
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
        val json = data?.let { Klaxon().parse<info>(it) }
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
            @SerializedName("get upuser live status")
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

        data class search(
            @SerializedName("code")
            val code: Int = 0,
            @SerializedName("data")
            val `data`: Data = Data(),
            @SerializedName("message")
            val message: String = "",
            @SerializedName("ttl")
            val ttl: Int = 0
        )


        var result = mutableMapOf<Long, String>()
        val data = get(
            "https://api.bilibili.com/x/web-interface/search/type?context=&search_type=bili_user&page=1&order=&category_id=&user_type=&order_sort=&changing=mid&__refresh__=true&_extra=&highlight=1&single_column=0&keyword=" + URLEncoder.encode(
                string,
                "UTF-8"
            )
        )
        if (data == null) {
            return mutableMapOf<Long, String>()
        } else {
            val json = Klaxon()
                .parse<search>(data)
            val r = json!!.data.result
            r.forEach {
                result.put(it.mid.toLong(), it.uname)
            }
            return result
        }
    }

    fun run(bot: Bot, group: Group) {
        if (this.bot == null) {
            this.bot = bot
        }
        groups.add(group)
        GlobalScope.launch {
            while (true) {
                delay(10 * 1000)
                ids.forEach {
                    delay(1000)
                    groups.forEach { group ->
                        val live = getLive(it.key)
                        if (live.stat == 1) group.sendMessage(group.uploadImage(URL(live.cover)).plus(live.message))
                    }
                }
            }
        }
    }

    fun getLive(uid: Long): live {
        var result = StringBuilder()

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

        data class live(
            val code: Int,
            val `data`: Data,
            val message: String,
            val ttl: Int
        )

        var d = 0
        var data = get("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=${uid}")
        var json: live? = Klaxon().parse<live>(data!!)
        if (json!!.data.liveStatus != status[uid]) {
            status.put(uid, json.data.liveStatus)
            if (json.data.liveStatus == 1) {
                result.append("${ids[uid]}直播啦！直播标题是${json.data.title},快到${json.data.url} 看8")
                d = 1
            }
        }
        Logger.getLogger("ddji").info("获取成功！$data")
        return live(cover = json.data.cover, message = result.toString(), stat = d)
    }

    fun getAv(Aid: Int) {
        TODO()
    }

    fun getBv(Bvid: String) {
        TODO()
    }

    data class live(
        val stat: Int,
        val cover: String,
        val message: String
    )
}