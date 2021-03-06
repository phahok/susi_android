package org.fossasia.susi.ai.skills.skilllisting

import org.fossasia.susi.ai.data.SkillListingModel
import org.fossasia.susi.ai.data.contract.ISkillListingModel
import org.fossasia.susi.ai.helper.Constant
import org.fossasia.susi.ai.helper.PrefManager
import org.fossasia.susi.ai.rest.responses.susi.ListGroupsResponse
import org.fossasia.susi.ai.rest.responses.susi.ListSkillsResponse
import org.fossasia.susi.ai.rest.responses.susi.SkillData
import org.fossasia.susi.ai.skills.skilllisting.contract.ISkillListingPresenter
import org.fossasia.susi.ai.skills.skilllisting.contract.ISkillListingView
import retrofit2.Response
import timber.log.Timber

/**
 * Skill Listing Presenter
 *
 * Created by chiragw15 on 15/8/17.
 */
class SkillListingPresenter : ISkillListingPresenter,
        ISkillListingModel.OnFetchGroupsFinishedListener, ISkillListingModel.OnFetchSkillsFinishedListener {

    private var skillListingModel: ISkillListingModel = SkillListingModel()
    private var skillListingView: ISkillListingView? = null
    private var count = 1
    var skills: ArrayList<Pair<String, List<SkillData>>> = ArrayList()
    private var groupsCount = 0
    private var groups: List<String> = ArrayList()

    override fun onAttach(skillListingView: ISkillListingView) {
        this.skillListingView = skillListingView
    }

    override fun getGroups(swipeToRefreshActive: Boolean) {
        skillListingView?.visibilityProgressBar(true)
        skillListingModel.fetchGroups(this)
    }

    override fun onGroupFetchSuccess(response: Response<ListGroupsResponse>) {
        if (response.isSuccessful && response.body() != null) {
            Timber.d("GROUPS FETCHED")
            groupsCount = response.body().groups.size
            groups = response.body().groups
            skillListingModel.fetchSkills(groups[0], PrefManager.getString(Constant.LANGUAGE, Constant.DEFAULT), this)
        } else {
            Timber.d("GROUPS NOT FETCHED")
            skillListingView?.visibilityProgressBar(false)
            skillListingView?.displayError()
        }
    }

    override fun onGroupFetchFailure(t: Throwable) {
        skillListingView?.visibilityProgressBar(false)
        skillListingView?.displayError()
    }

    override fun onSkillFetchSuccess(response: Response<ListSkillsResponse>, group: String) {
        skillListingView?.visibilityProgressBar(false)
        if (response.isSuccessful && response.body() != null) {
            Timber.d("SKILLS FETCHED")
            val responseSkillMap = response.body().filteredSkillsData
            if (responseSkillMap.isNotEmpty()) {
                skills.add(Pair(group, responseSkillMap))
                skillListingView?.updateAdapter(skills)
            }
            if (count != groupsCount) {
                skillListingModel.fetchSkills(groups[count], PrefManager.getString(Constant.LANGUAGE, Constant.DEFAULT), this)
                count++
            }
        } else {
            Timber.d("SKILLS NOT FETCHED")
            skillListingView?.visibilityProgressBar(false)
            skillListingView?.displayError()
        }
    }

    override fun onSkillFetchFailure(t: Throwable) {
        skillListingView?.visibilityProgressBar(false)
        skillListingView?.displayError()
    }

    override fun onDetach() {
        skillListingModel.cancelFetch()
        skillListingView = null
    }
}
