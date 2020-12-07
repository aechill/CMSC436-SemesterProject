package com.example.whobuiltthat

import android.graphics.Color
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompanyAdapter (private val mCompanies : ArrayList<Company>, private val listener: CompanyClickedListener) : RecyclerView.Adapter<CompanyAdapter.ViewHolder>() {
    interface CompanyClickedListener {
        fun onCompanyClicked(pos: Int)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CompanyAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false), listener)
    }

    inner class ViewHolder(private val currView: View, private val ccl: CompanyClickedListener) : RecyclerView.ViewHolder(currView),
        View.OnClickListener {
        val companyName = currView.findViewById(R.id.company_name_title) as TextView
        init {
            currView.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            ccl.onCompanyClicked(adapterPosition)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currCompany = mCompanies[position]
        //holder.companyName.text = Html.fromHtml(("<a href=\"\">" + currCompany.name + "</a>"), 0)
        holder.companyName.text = Html.fromHtml(("<b><u>" + currCompany.name + "</u></b>"), 0)
    }

    override fun getItemCount(): Int {
        return mCompanies.size
    }
}