package com.lidesheng.hyperlyric.utils

data class ChangelogItem(
    val version: String,
    val title: String,
    val summary: String
)

object ChangelogManager {
    fun getChangelog(): List<ChangelogItem> {
        return listOf(
            ChangelogItem(
                version = "5.7-1928",
                title = "本次更新日志如下",
                summary = "- 新增音乐信息“居中显示”功能\n" +
                        "- 新增“更新日志”页面\n" +
                        "- 新增“歌曲信息”滚动\n" +
                        "- 新增超级岛“边缘光效封面色”\n" +
                        "- 主页“特殊功能”新增模块激活状态检测\n" +
                        "- 歌曲信息“滚动延迟”和“滚动循环间隔”最大值提升到10秒\n" +
                        "- 悬浮底栏新增高光样式\n" +
                        "- 合并“歌词提供服务”和“歌词白名单”页面，优化白名单逻辑，无需手动添加\n" +
                        "- 优化翻译和ai翻译功能\n" +
                        "- 优化主页底栏、权限弹窗、日志等级的样式\n" +
                        "- 优化日志扫描内容\n" +
                        "- 优化应用架构\n" +
                        "- 应用内全部toast替换成snackbar\n" +
                        "- 优化模糊效果\n" +
                        "- 优化组件间距\n" +
                        "- 优化性能"
            ),
            ChangelogItem(
                version = "",
                title = "",
                summary = "v5.7之前的更新内容请前往酷安或github查看"
            )
        )
    }
}
