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
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutUserListBinding
import kr.co.hs.sudoku.extension.Number.dp

class UserListBottomSheetDialog : BottomSheetDialogFragment() {
    companion object {
        fun show(fragmentManager: FragmentManager) =
            UserListBottomSheetDialog()
                .show(fragmentManager, UserListBottomSheetDialog::class.java.name)
    }

    private lateinit var binding: LayoutUserListBinding
    private val viewModel: UserProfileViewModel
            by viewModels { (requireActivity() as Activity).getUserProfileProviderFactory() }

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
            val itemAdapter = UserListAdapter()
            adapter = itemAdapter
            viewModel.onlineUserList.observe(viewLifecycleOwner) {
                val list = buildList {
                    it.toMutableList()
                        .also { mutableList ->
                            mutableList
                                .find { it.uid == FirebaseAuth.getInstance().currentUser?.uid }
                                ?.let { pick ->
                                    mutableList.remove(pick)
                                    add(UserListItem.Header(getString(R.string.user_list_label_header_my_info)))
                                    add(UserListItem.User(pick))
                                }
                        }
                        .let {
                            if (it.isEmpty()) {
                                add(UserListItem.Header(getString(R.string.user_list_label_header_others)))
                                add(UserListItem.EmptyMessage)
                            } else {
                                add(UserListItem.Header(getString(R.string.user_list_label_header_others)))
                                addAll(it.map { UserListItem.User(it) })
                            }
                        }
                }

                itemAdapter.submitList(list)
            }
        }

        viewModel.requestOnlineUserList()
    }


}