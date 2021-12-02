package uk.co.rhul.r14.letamagotchijos;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

public class BluetoothDeviceRecycleViewer extends RecyclerView.Adapter<BluetoothDeviceRecycleViewer.ViewHolder> {

    private final List<BluetoothDevice> deviceList;
    private int clicked;
    private CompoundButton clickedBtn;

    /**
     * Initialises the dataset of the adapter.
     *
     * @param devices -> the devices that the app can see
     * @since 1.0
     */
    public BluetoothDeviceRecycleViewer(List<BluetoothDevice> devices) {
        this.deviceList = devices;
        this.clicked = -1;
        this.clickedBtn = null;
    }

    public BluetoothDevice getClicked() {
        return this.clicked != -1 ? this.deviceList.get(this.clicked) : null;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BluetoothDeviceRecycleViewer.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_device_fragment, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceRecycleViewer.ViewHolder holder, int position) {
        MaterialRadioButton radioButton = holder.getView().findViewById(R.id.bluetooth_device_name);
        radioButton.setChecked(false);
        radioButton.setText(this.deviceList.get(position).getName());

        radioButton.setOnCheckedChangeListener((v, checked) -> {
            if (checked) {
                if (this.clickedBtn != null && (int) this.clickedBtn.getTag() != (int) v.getTag()) {
                    this.clickedBtn.setChecked(false);
                    Log.i(BluetoothDeviceRecycleViewer.class.toString(), "Unselected an old device");
                }

                v.setChecked(true);
                this.clickedBtn = v;
                this.clicked = position;

                Log.i(BluetoothDeviceRecycleViewer.class.toString(), "Selected a new device");
            }
        });

        radioButton.setTag(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public View getView() {
            return this.view;
        }

    }
}
