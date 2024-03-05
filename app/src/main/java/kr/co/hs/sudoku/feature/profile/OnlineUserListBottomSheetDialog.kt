package kr.co.hs.sudoku.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutUserListBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.feature.multi.MultiPlayCreateActivity
import kr.co.hs.sudoku.model.user.ProfileEntity

@AndroidEntryPoint
class OnlineUserListBottomSheetDialog : BottomSheetDialogFragment(),
    ProfilePopupMenu.OnPopupMenuItemClickListener {
    companion object {
        fun show(fragmentManager: FragmentManager) =
            OnlineUserListBottomSheetDialog()
                .show(fragmentManager, OnlineUserListBottomSheetDialog::class.java.name)
    }

    private lateinit var binding: LayoutUserListBinding
    private val viewModel: OnlineUserListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutUserListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewList) {
            layoutManager = LinearLayoutManager(context)
            addVerticalDivider(thickness = 5.dp)
            val itemAdapter = OnlineUserListAdapter(this@OnlineUserListBottomSheetDialog)
            adapter = itemAdapter
            viewModel.onlineUserList.observe(viewLifecycleOwner) {
                itemAdapter.submitList(it.toOnlineUserList())
            }
        }

        viewModel.requestOnlineUserList()
    }

    private fun List<ProfileEntity.OnlineUserEntity>.toOnlineUserList() =
        ArrayList<OnlineUserListItem>().also { output ->
            toMutableList()
                .also { mutableList ->
                    mutableList
                        .find { it.uid == FirebaseAuth.getInstance().currentUser?.uid }
                        ?.let { pick ->
                            mutableList.remove(pick)
                            output.add(OnlineUserListItem.Header(getString(R.string.user_list_label_header_my_info)))
                            output.add(OnlineUserListItem.UserForMe(pick))
                        }
                }
                .let {
                    if (it.isEmpty()) {
                        output.add(OnlineUserListItem.Header(getString(R.string.user_list_label_header_others)))
                        output.add(OnlineUserListItem.EmptyMessage)
                    } else {
                        output.add(OnlineUserListItem.Header(getString(R.string.user_list_label_header_others)))
                        output.addAll(it.map { OnlineUserListItem.User(it) })
                    }
                }
        }

    override fun onClickProfile(uid: String) =
        ProfileBottomSheetDialog.show(childFragmentManager, uid)

    override fun onClickInviteMultiPlay(uid: String, displayName: String) {
        val title = getString(R.string.multi_play_invite_confirm_title)
        val message = getString(R.string.multi_play_invite_confirm_message, displayName)
        (requireActivity() as Activity).showConfirm(title, message) {
            if (it) {
                dismiss()
                MultiPlayCreateActivity.start(requireActivity(), uid)
            }
        }
    }
}