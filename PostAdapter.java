package app.phedev.eramasjid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import app.phedev.eramasjid.R;
import app.phedev.eramasjid.databinding.ContentPostBinding;
import app.phedev.eramasjid.helper.StaticValue;
import app.phedev.eramasjid.helper.TimeStampConverter;
import app.phedev.eramasjid.model.Actor;
import app.phedev.eramasjid.model.PostItem;
import app.phedev.eramasjid.model.Social;

/**
 * Created by phedev in 2019.
 */
public class PostAdapter extends
        RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private static final String TAG = PostAdapter.class.getSimpleName();

    private Context context;
    private List<PostItem> list;
    private PostAdapterCallback mAdapterCallback;
    private PostLikeCallback mLikeCallback;
    private PostLikeDataCallback mLikeDataCallback;
    private boolean isLike;

    public PostAdapter(Context context, PostAdapterCallback adapterCallback) {
        this.context = context;
        this.mAdapterCallback = adapterCallback;
    }

    public PostAdapter(Context context, PostAdapterCallback mAdapterCallback, PostLikeCallback mLikeCallback) {
        this.context = context;
        this.mAdapterCallback = mAdapterCallback;
        this.mLikeCallback = mLikeCallback;
    }

    public PostAdapter(Context context, PostAdapterCallback mAdapterCallback, PostLikeCallback mLikeCallback,
                       PostLikeDataCallback mLikeDataCallback) {
        this.context = context;
        this.mAdapterCallback = mAdapterCallback;
        this.mLikeCallback = mLikeCallback;
        this.mLikeDataCallback = mLikeDataCallback;
    }

    public PostAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ContentPostBinding.inflate(LayoutInflater.from(parent.getContext())));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PostItem item = list.get(position);

        holder.binding.postName.setText(item.getActor().getName());
        if (item.getCaption()!=null){
            holder.binding.postTextContent.setText(item.getCaption());
        }
        holder.binding.postRelativeDate.setText(TimeStampConverter.relativeDate((long)item.getUpdatedAt()));

        holder.binding.postCommentData.setText(String.format("%s Komentar", String.valueOf(
                item.getSocial().getCommentsCount()
        )));

        holder.binding.postLikeData.setText(String.valueOf(item.getSocial().getLikesCount()));
        holder.binding.postLikeData.setOnClickListener(v -> {
            if (item.getSocial().getLikesCount()==0){
                return;
            }
            mLikeDataCallback.onPostLikeDataClicked(item.getId());
        });

        holder.binding.postImageMore.setOnClickListener(v -> {
//            Toast.makeText(context, "Belum ada menu", Toast.LENGTH_SHORT).show();
        });

        holder.binding.postLikeBtn.setOnClickListener(v -> {
            mLikeCallback.onPostLikeClicked(item.getId(), holder.binding.postLikeBtn,
                    holder.binding.postLikeData);

        });

        if (item.getLike()){
            holder.binding.postLikeBtn.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(R.drawable.ic_icon_love_active), null, null, null
            );
        }

        holder.binding.postCommentData.setOnClickListener(v -> {
            mAdapterCallback.onRowPostAdapterClicked(item,item.getActor(), item.getSocial());
        });

        holder.binding.postShareBtn.setOnClickListener(v -> {
//            Toast.makeText(context, "Share detected", Toast.LENGTH_SHORT).show();
        });

        holder.binding.postCommentBtn.setOnClickListener(v -> {
            mAdapterCallback.onRowPostAdapterClicked(item, item.getActor(), item.getSocial());
        });

        holder.binding.postImageProfil.setOnClickListener(v -> {
            mAdapterCallback.onPostNameAdapterClicked(item);
        });

        holder.binding.postName.setOnClickListener(v -> {
            mAdapterCallback.onPostNameAdapterClicked(item);
        });



        //profile photo
        RequestOptions requestOptionsProfil = new RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.bg_shape_oval)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        if (item.getActor().getFoto()!=null){
            Glide.with(context)
                    .setDefaultRequestOptions(requestOptionsProfil)
                    .load(StaticValue.BASE_IMAGE_URL+item.getActor().getFoto().getThumb())
                    .into(holder.binding.postImageProfil);
        } else {
            Glide.with(context)
                    .setDefaultRequestOptions(requestOptionsProfil)
                    .load("")
                    .into(holder.binding.postImageProfil);
        }

        if (item.getImage() == null||item.getImage().size()==0){
            holder.binding.postImageContent.setVisibility(View.GONE);
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_logo_putih)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        //content image
        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(StaticValue.BASE_IMAGE_URL+item.getImage().get(0).getLink())
                .thumbnail(Glide.with(context).load(StaticValue.BASE_IMAGE_URL+item.getImage().get(0).getLazy()))
                .into(holder.binding.postImageContent);


    }

    @Override
    public int getItemCount() {
        int size;
        if (list == null) {
            size = 0;
        } else {
            size = list.size();
        }
        return size;
    }

    public void clear() {
        int size = this.list.size();
        this.list.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void setList(List<PostItem> listModel) {
        this.list = listModel;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ContentPostBinding binding;
        public ViewHolder(ContentPostBinding contentPostBinding) {
            super(contentPostBinding.getRoot());
            binding = contentPostBinding;
        }
    }

    public interface PostAdapterCallback {
        void onRowPostAdapterClicked(PostItem post, Actor actor, Social social);
        void onPostNameAdapterClicked(PostItem postItem);
    }

    public interface PostLikeCallback {
        void onPostLikeClicked(int id, TextView likeBtn, TextView likeData);
    }

    public interface PostLikeDataCallback {
        void onPostLikeDataClicked(int id);
    }
}