package com.example.maintainmate

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VehicleAdapter(
    private val context: Context,
    private val vehicles: MutableList<Vehicle>,
    private val isMaintenanceMode: Boolean, // Determines button configuration
    private val onMaintenanceClicked: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val vehicleInfo: TextView = itemView.findViewById(R.id.vehicleInfo)
        private val maintenanceButton: Button = itemView.findViewById(R.id.maintenanceButton)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(vehicle: Vehicle) {
            vehicleInfo.text = "${vehicle.brand} ${vehicle.model} (${vehicle.year}), Mileage: ${vehicle.mileage} miles"

            // Configure buttons based on mode
            if (isMaintenanceMode) {
                maintenanceButton.visibility = View.VISIBLE
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE

                maintenanceButton.setOnClickListener {
                    onMaintenanceClicked(vehicle)
                }
            } else {
                maintenanceButton.visibility = View.GONE
                editButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE

                editButton.setOnClickListener {
                    val intent = Intent(context, AddVehicleActivity::class.java).apply {
                        putExtra("VEHICLE_DOC_ID", vehicle.id)
                        putExtra("BRAND", vehicle.brand)
                        putExtra("MODEL", vehicle.model)
                        putExtra("YEAR", vehicle.year)
                        putExtra("MILEAGE", vehicle.mileage)
                        putExtra("VIN", vehicle.vin)
                    }
                    context.startActivity(intent)
                }

                deleteButton.setOnClickListener {
                    // Call delete function for this vehicle
                    deleteVehicle(vehicle)
                }
            }
        }

        private fun deleteVehicle(vehicle: Vehicle) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(userId)
                    .collection("vehicles")
                    .document(vehicle.id)
                    .delete()
                    .addOnSuccessListener {
                        // Remove vehicle from the list and update adapter
                        vehicles.remove(vehicle)
                        notifyItemRemoved(adapterPosition)
                        Toast.makeText(context, "Vehicle deleted!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete vehicle: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateData(newVehicles: List<Vehicle>) {
        vehicles.clear()
        vehicles.addAll(newVehicles)
        notifyDataSetChanged()
    }
}
