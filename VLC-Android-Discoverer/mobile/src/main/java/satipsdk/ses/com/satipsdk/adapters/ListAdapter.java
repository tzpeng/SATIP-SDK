package satipsdk.ses.com.satipsdk.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import satipsdk.ses.com.satipsdk.R;
import satipsdk.ses.com.satipsdk.databinding.ListItemBinding;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements View.OnFocusChangeListener{

    private static final String TAG = "ListAdapter";

    public static final int TYPE_SERVER = 0;
    public static final int TYPE_CHANNEL = 1;
    public static final int TYPE_CHANNEL_LIST = 2;
    public static final int TYPE_SERVER_CUSTOM = 3;
    public static final int TYPE_CHANNEL_LIST_CUSTOM = 4;

    private ArrayList<Item> mItemList;
    private LayoutInflater mInflater;
    private SparseIntArray mItemsIndex = new SparseIntArray();
    private ItemClickCb mItemClickCb;

    public interface ItemClickCb {
        void onItemClick(int position, Item item);
    }

    public ListAdapter() {
        this(new ArrayList<Item>());
    }

    public ListAdapter(ArrayList<Item> serverList) {
        super();
        mItemList = serverList;
    }

    public void setItemClickHandler(ItemClickCb itemClickCb) {
        mItemClickCb = itemClickCb;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null)
            mInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder((ListItemBinding) DataBindingUtil.inflate(mInflater, R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.setItem(mItemList.get(position));
        if (mItemList.get(position).logoUrl != null)
            Glide.with(holder.itemView.getContext())
            .load(holder.binding.getItem().logoUrl)
            .fitCenter()
            .crossFade()
            .into(holder.binding.itemLogo);
        else
            holder.binding.itemLogo.setVisibility(View.GONE);
        holder.itemView.requestFocus();
    }

    public void add(Item item) {
        mItemList.add(item);
        notifyItemInserted(mItemList.size()-1);
    }

    public void add(int position, Item item) {
        int actualPosition = mItemList.size();
        mItemList.add(item);
        mItemsIndex.put(position, actualPosition);
        notifyItemInserted(actualPosition);
    }

    public void remove(int position) {
        mItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeServer(int position) {
        int actualPosition = mItemsIndex.get(position);
        if (actualPosition >= mItemList.size() || actualPosition < 0)
            return;
        mItemList.remove(actualPosition);
        notifyItemRemoved(actualPosition);
        mItemsIndex.delete(position);
    }

    public void clear() {
        if (mItemList.isEmpty())
            return;
        mItemList.clear();
        notifyDataSetChanged();
    }

    public ArrayList<Item> getAll() {
        return mItemList;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus)
            v.setBackground(v.getResources().getDrawable(R.drawable.gradient_cloud));
        else {
            v.setBackgroundColor(v.getResources().getColor(R.color.pure_white));
            v.setFocusableInTouchMode(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            v.setElevation(hasFocus ? 10.0f : 0.0f);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ListItemBinding binding;

        public ViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
            binding.itemDelete.setOnClickListener(this);
            itemView.setOnFocusChangeListener(ListAdapter.this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickCb != null) {
                if (v.getId() == itemView.getId()) {
                    v.setFocusableInTouchMode(true);
                    v.requestFocus();
                    mItemClickCb.onItemClick(getAdapterPosition(), binding.getItem());
                } else if (v.getId() == binding.itemDelete.getId()) {
                    final int position = getAdapterPosition();
                    final Item item = binding.getItem();
                    remove(position);
                    Snackbar.make(v, item.type == TYPE_SERVER_CUSTOM ? R.string.server_removed : R.string.channel_list_removed,
                            Snackbar.LENGTH_LONG).setAction(android.R.string.cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            add(position, item);
                        }
                    }).show();
                }
            }
        }
    }

    public static class Item {
        public int type;
        public String title, logoUrl;
        public Uri uri;

        public Item(int type, String title, Uri uri, String logoUrl) {
            this.type = type;
            this.title = title;
            this.logoUrl = type == TYPE_CHANNEL ? generateLogoUrl(title) : logoUrl;
            this.uri = uri;
        }

        String generateLogoUrl(String title) {
            StringBuilder sb = new StringBuilder("http://www.satip.info/sites/satip/files/files/Playlists/Channellogos/")
                    .append(title.replace(' ', '-').replace('.', '-').toLowerCase())
                    .append(".png");
            return sb.toString();
        }
    }
}
