package app.phedev.eramasjid.ui.main.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import app.phedev.eramasjid.R;
import app.phedev.eramasjid.adapter.NewsAdapter;
import app.phedev.eramasjid.adapter.PostAdapter;
import app.phedev.eramasjid.databinding.FragmentHomeBinding;
import app.phedev.eramasjid.helper.AppPreference;
import app.phedev.eramasjid.helper.StaticValue;
import app.phedev.eramasjid.model.Actor;
import app.phedev.eramasjid.model.News;
import app.phedev.eramasjid.model.PostItem;
import app.phedev.eramasjid.model.Social;
import app.phedev.eramasjid.model.User;
import app.phedev.eramasjid.ui.main.ListProfileActivity;
import app.phedev.eramasjid.ui.main.ProfileMasjidActivity;
import app.phedev.eramasjid.ui.main.ProfileUserActivity;
import app.phedev.eramasjid.ui.main.StreamingActivity;
import app.phedev.eramasjid.ui.main.profil.ProfileViewModel;

public class HomeFragment extends Fragment implements NewsAdapter.NewsAdapterCallback,
        PostAdapter.PostAdapterCallback, PostAdapter.PostLikeCallback, PostAdapter.PostLikeDataCallback
        {

            private static final int REQUEST_PERMISSION = 887;
            private HomeViewModel homeViewModel;
    private NewsAdapter newsAdapter;
    private NavController navController;
    private FragmentHomeBinding binding;
    private String path;
    private User user;
    private TextView likeBtn, likeData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        navController = Navigation.findNavController(view);

        homeViewModel.checkNetwork(getContext());

        //todo : if there is streaming inflate layout
        inflateStreamLayout(false);

        newsAdapter = new NewsAdapter(getContext(), this);
        binding.recyclerNews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerNews.setAdapter(newsAdapter);

        binding.addPostBtn.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AddPostActivity.class), 200);
        });

        binding.imageStreamingClose.setOnClickListener(v -> {
            inflateStreamLayout(false);
        });

        binding.searchBtn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ListProfileActivity.class));
        });

        binding.imagePost.setOnClickListener(v -> {
            if (!checkPermission()){
                reqPermission();
                return;
            }
            Intent intent  = new Intent(getContext(), AddPostActivity.class);
            intent.putExtra(StaticValue.KEY_POST, "post");
            startActivity(intent);
        });

        homeViewModel.getErrorData().observe(getViewLifecycleOwner(), s -> {
            if (s== null){
                return;
            }
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });

        homeViewModel.getIsConnecting().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean==null){
                return;
            }
            if (aBoolean){
                homeViewModel.init();
            } else {
                Toast.makeText(getContext(), "No Data Connection", Toast.LENGTH_SHORT).show();
            }
        });

        homeViewModel.getNewsData().observe(getViewLifecycleOwner(), news -> {
            if (news == null){
                return;
            }
            newsAdapter.setList(news);
            newsAdapter.notifyDataSetChanged();
        });

        homeViewModel.getCurrentPhotoPath().observe(getViewLifecycleOwner(), s -> {
            if (s==null){
                return;
            }
            String[] code = s.split(",");
            path = s;
        });

        homeViewModel.getIsFromServer().observe(getViewLifecycleOwner(),aBoolean -> {
            if (aBoolean == null){
                return;
            }
            RequestOptions requestOptions = new RequestOptions()
                    .circleCrop()
                    .error(R.drawable.bg_shape_oval)
                    .placeholder(R.drawable.bg_shape_oval);

            if (aBoolean){
                byte[] imageByteArray = Base64.decode(path, Base64.DEFAULT);
                Glide.with(getContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(imageByteArray)
                        .into(binding.profilImg);
            } else {
                Glide.with(getContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(path)
                        .into(binding.profilImg);
            }
        });

        homeViewModel.getIsLikeData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean==null){
                return;
            }
            if (!aBoolean){
                likeBtn.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_icon_love),null,
                        null,null);
                int totLike = Integer.parseInt(likeData.getText().toString());
                likeData.setText(String.valueOf(totLike-1));
            } else {
               likeBtn.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_icon_love_active),null,
                        null,null);
                int totLike = Integer.parseInt(likeData.getText().toString());
                likeData.setText(String.valueOf(totLike+1));
            }
        });

        binding.recyclerPost.setLayoutManager(new LinearLayoutManager(getContext()));
        PostAdapter postAdapter = new PostAdapter(getContext(), this, this, this);
        binding.recyclerPost.setAdapter(postAdapter);
        homeViewModel.getPostData().observe(getViewLifecycleOwner(), posts -> {
            if (posts==null||posts.size()==0){
                binding.noPostData.setVisibility(View.VISIBLE);
                return;
            }
            binding.noPostData.setVisibility(View.GONE);
            postAdapter.setList(posts);
        });


        AppPreference appPreference = new AppPreference(getContext());
        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user==null){
                return;
            }
            this.user = user;
            Log.d(StreamingActivity.class.getSimpleName(), "url_stream: " +
                    StaticValue.urlStreaming(user.getUsername(),appPreference.getIdToken()));
        });

        binding.streamBtn.setOnClickListener(v -> {
            if (user == null){
                Toast.makeText(getContext(), "error get user, try later", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), StreamingActivity.class);
            intent.putExtra(StaticValue.KEY_USER, user);
            startActivity(intent);
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (appPreference.getMasjidId()!=0){
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                    .setTitle("Keluar Aplikasi")
                                    .setMessage("Apakah kamu yakin akan keluar?")
                                    .setPositiveButton("Ya", (dialog, which) -> {
                                        requireActivity().finishAndRemoveTask();
                                        System.exit(0);
                                    })
                                    .setNegativeButton("Tidak", (dialog, which) -> {
                                        dialog.dismiss();
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                        requireActivity().onBackPressed();
                    }
                });

    }


    @Override
    public void onRowNewsAdapterClicked(int position, News news) {
        Bundle bundle = new Bundle();
        bundle.putString(StaticValue.KEY_WEBVIEW, news.getLink());
        bundle.putString(StaticValue.NEWS_KEY_TITLE, "Era News");
        navController.navigate(R.id.action_navigation_home_to_navigation_news, bundle);
    }

    private void inflateStreamLayout(boolean inflate){
        if (inflate){
            binding.layoutStreaming.setVisibility(View.VISIBLE);
            binding.imageTriangle.setVisibility(View.VISIBLE);
        } else {
            binding.layoutStreaming.setVisibility(View.GONE);
            binding.imageTriangle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRowPostAdapterClicked(PostItem post, Actor actor, Social social) {
        Intent intent = new Intent(getContext(), DetailPostActivity.class);
        intent.putExtra(StaticValue.KEY_ID, post.getId());
        startActivity(intent);
    }

    @Override
    public void onPostNameAdapterClicked(PostItem postItem) {
        //if actor type is 1 then it's profile detail page for user, 2 for masjid
        if (postItem.getActorType() == 1){
            Intent intent = new Intent(getContext(), ProfileUserActivity.class);
            intent.putExtra(StaticValue.KEY_ID, postItem.getActorId());
            startActivity(intent);
        } else {
            Intent intent = new Intent(getContext(), ProfileMasjidActivity.class);
            intent.putExtra(StaticValue.KEY_ID, postItem.getActorId());
            startActivity(intent);
        }
    }

    @Override
    public void onPostLikeClicked(int id, TextView likeBtn, TextView likeData) {
        homeViewModel.likePost(id);
        this.likeBtn = likeBtn;
        this.likeData = likeData;
    }

            private void reqPermission(){
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);
            }
            private boolean checkPermission(){
                return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==200){
            if (resultCode== Activity.RESULT_OK){
                homeViewModel.postDataList();
            }
        }
    }

            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode==REQUEST_PERMISSION){
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Intent intent  = new Intent(getContext(), AddPostActivity.class);
                        intent.putExtra(StaticValue.KEY_POST, "post");
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
    public void onPostLikeDataClicked(int id) {
        Intent intent = new Intent(getContext(), ListProfileActivity.class);
        intent.putExtra(StaticValue.KEY_ID, id);
        startActivity(intent);
    }


}