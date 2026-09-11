package com.example.desafiopractico2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DestinoAdapter(
    private val listaDestinos: MutableList<DestinoTuristico>,
    private val onItemClick: (DestinoTuristico) -> Unit
) : RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    class DestinoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ImageView = view.findViewById(R.id.ivFotoDestino)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreDestino)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioDestino)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionDestino)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destino, parent, false)
        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = listaDestinos[position]

        holder.tvNombre.text = destino.nombre
        holder.tvPrecio.text = "$${destino.precio}"
        holder.tvDescripcion.text = destino.descripcion


        Glide.with(holder.itemView.context)
            .load(destino.imagenUrl)
            .centerCrop()
            .into(holder.ivFoto)

        holder.itemView.setOnClickListener {
            onItemClick(destino)
        }
    }

    override fun getItemCount(): Int = listaDestinos.size


    fun actualizarLista(nuevaLista: List<DestinoTuristico>) {
        listaDestinos.clear()
        listaDestinos.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}