package geographic.boger.me.nationalgeographic.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutCompat
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ScrollView
import android.widget.TextView
import geographic.boger.me.nationalgeographic.BuildConfig
import geographic.boger.me.nationalgeographic.R
import geographic.boger.me.nationalgeographic.core.DisplayProvider
import geographic.boger.me.nationalgeographic.core.NGRumtime
import geographic.boger.me.nationalgeographic.main.ngdetail.NGDetailFragment
import geographic.boger.me.nationalgeographic.main.selectdate.SelectDateAlbumData
import geographic.boger.me.nationalgeographic.main.selectdate.SelectDateFragment

class MainActivity : AppCompatActivity(), IActivityMainUIController {

    private enum class MenuState {
        OPEN, CLOSE, BACK
    }

    private val mPresenter by lazy {
        MainActivityPresenter()
    }

    private val tvNGTitle by lazy {
        findViewById(R.id.tv_activity_main_ng_title) as TextView
    }

    private val tvNGMenu by lazy {
        findViewById(R.id.tv_activity_main_ng_menu) as TextView
    }

    private val ablTitle by lazy {
        findViewById(R.id.abl_activity_main_ng_title) as AppBarLayout
    }

    private val llcOverlayMenu by lazy {
        findViewById(R.id.llc_activity_main_overlay_menu) as LinearLayoutCompat
    }

    private val svOverlayMenu by lazy {
        findViewById(R.id.sv_activity_main_overlay_menu) as ScrollView
    }

    private var mPendingMenuAnimator: Animator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        mPresenter.init(this)
    }

    fun showSelectDateContent(listener: (SelectDateAlbumData) -> Unit) {
        fragmentManager.beginTransaction()
                .add(R.id.cfl_activity_main_ng_content, SelectDateFragment(listener), SelectDateFragment.TAG)
                .commit()
    }

    fun showNGDetailContent(data: SelectDateAlbumData) {
        val offlineData =
                if (data.id == "unset") NGRumtime.favoriteNGDetailDataSupplier.getNGDetailData() else null
        if (offlineData != null && offlineData.picture.isEmpty()) {
            Snackbar.make(
                    findViewById(R.id.cfl_activity_main_ng_content_full),
                    getString(R.string.tip_empty_favorite),
                    Snackbar.LENGTH_SHORT)
                    .show()
            return
        }
        val df = NGDetailFragment(
                data = data,
                controller = this,
                offlineData = offlineData)
        fragmentManager.beginTransaction()
                .add(R.id.cfl_activity_main_ng_content_full, df, NGDetailFragment.TAG)
                .addToBackStack(null)
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .commit()
        ablTitle.setExpanded(true)
        setMenuState(MenuState.BACK)
    }

    override fun onBackPressed() {
        if (fragmentManager.popBackStackImmediate()) {
            setMenuState(MenuState.CLOSE)
        } else {
            super.onBackPressed()
        }
    }

    private fun initView() {
        tvNGTitle.typeface = DisplayProvider.primaryTypeface
        tvNGMenu.typeface = DisplayProvider.iconFont
        ablTitle.postDelayed({
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }, 800)
        tvNGMenu.text = "\ue665"
        tvNGMenu.setOnClickListener(object : View.OnClickListener {

            private var state = MenuState.CLOSE

            override fun onClick(p0: View?) {
                if ("\ue6d8" == tvNGMenu.text) {
                    state = MenuState.CLOSE
                    onBackPressed()
                } else if (state == MenuState.OPEN) {
                    state = MenuState.CLOSE
                    setMenuState(state)
                } else {
                    state = MenuState.OPEN
                    setMenuState(state)
                }
            }
        })
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue600",
                name = getString(R.string.menu_language_settings),
                iconRight = "\ue615",
                value = "简",
                listener = {
                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue6a2",
                name = getString(R.string.menu_clean_cache),
                iconRight = "\ue615",
                value = "未检测到",
                listener = {
                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue609",
                name = getString(R.string.menu_download_offline),
                iconRight = "\ue615",
                value = "",
                listener = {
                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue65b",
                name = "动态壁纸",
                iconRight = "\ue615",
                value = "",
                listener = {
                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue60a",
                name = "检查更新",
                iconRight = "\ue615",
                value = "v${BuildConfig.VERSION_NAME}",
                listener = {
                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue62a",
                name = "免责声明",
                iconRight = "\ue615",
                value = "",
                listener = {
                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ued05",
                name = "关于作者",
                iconRight = "\ue615",
                value = "波哥",
                listener = {
                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
                }))
    }

    private fun createOverlayMenuItem(iconLeft: String, name: String, value: String, iconRight: String? = null, listener: (View) -> Unit): View {
        val v = layoutInflater.inflate(R.layout.item_overlay_menu, null)
        v.setOnClickListener(listener)
        val tvIconLeft = v.findViewById<TextView>(R.id.tv_item_overlay_menu_left_icon)
        tvIconLeft.typeface = DisplayProvider.iconFont
        tvIconLeft.text = iconLeft
        val tvIconRight = v.findViewById<TextView>(R.id.tv_item_overlay_menu_right_icon)
        tvIconRight.typeface = DisplayProvider.iconFont
        if (TextUtils.isEmpty(iconRight)) {
            tvIconRight.visibility = View.INVISIBLE
        } else {
            tvIconRight.text = iconRight
        }
        val tvName = v.findViewById<TextView>(R.id.tv_item_overlay_menu_name)
        tvName.typeface = DisplayProvider.primaryTypeface
        tvName.text = name
        val tvValue = v.findViewById<TextView>(R.id.tv_item_overlay_menu_value)
        tvValue.typeface = DisplayProvider.primaryTypeface
        tvValue.text = value
        return v
    }

    override fun getTitleBar(): View {
        return ablTitle
    }

    private fun setMenuState(state: MenuState) {
        mPendingMenuAnimator?.cancel()
        val range = when (state) {
            MenuState.CLOSE -> arrayOf(tvNGMenu.rotation, 0f)
            else -> arrayOf(tvNGMenu.rotation, 360f)
        }
        val menuIconText = when (state) {
            MenuState.OPEN -> "\ue736"
            MenuState.CLOSE -> "\ue665"
            MenuState.BACK -> "\ue6d8"
        }
        val animSet = AnimatorSet()
        animSet.duration = 300
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                mPendingMenuAnimator = animation
                if (MenuState.OPEN == state) {
                    svOverlayMenu.visibility = View.VISIBLE
                }
                svOverlayMenu.pivotX = 0f
                svOverlayMenu.pivotY = 0f
            }

            override fun onAnimationEnd(animation: Animator?) {
                mPendingMenuAnimator = null
                if (state != MenuState.OPEN) {
                    svOverlayMenu.visibility = View.INVISIBLE
                }
                svOverlayMenu.pivotX = svOverlayMenu.measuredWidth / 2f
                svOverlayMenu.pivotY = svOverlayMenu.measuredHeight / 2f
            }
        })
        val aniMenuButton = ValueAnimator.ofFloat(*range.toFloatArray())
        aniMenuButton.interpolator = LinearInterpolator()
        aniMenuButton.addUpdateListener {
            val fraction = it.animatedFraction
            if (fraction > .5f) {
                tvNGMenu.alpha = (fraction - 0.5f) * 2
                if (menuIconText != tvNGMenu.text) {
                    tvNGMenu.text = menuIconText
                }
            } else {
                tvNGMenu.alpha = 1 - fraction * 2
            }
            tvNGMenu.rotation = it.animatedValue as Float
        }
        val aniBuilder = animSet.play(aniMenuButton)
        if (state != MenuState.BACK) {
            val aniMenuContent = ValueAnimator.ofFloat(*range.toFloatArray())
            aniMenuContent.interpolator = LinearInterpolator()
            aniMenuContent.addUpdateListener {
                svOverlayMenu.alpha = it.animatedValue as Float / 360
            }
            val aniRotation = ValueAnimator.ofFloat(*range.toFloatArray())
            aniRotation.interpolator = OvershootInterpolator()
            aniRotation.addUpdateListener {
                svOverlayMenu.rotationX = (1 - it.animatedValue as Float / 360) * 10
            }
            aniBuilder.with(aniMenuContent).with(aniRotation)
        }

        animSet.start()
    }
}
