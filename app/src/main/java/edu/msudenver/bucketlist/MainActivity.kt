package edu.msudenver.bucketlist

/*
 * CS3013 - Mobile App Dev. - Summer 2022
 * Instructor: Thyago Mota
 * Student(s): Brea Chaney and Alyssa Williams
 * Description: App 02 - MainActivity (controller) class
 */

import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.Console

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    private val ISO_FORMAT = DBHelper.ISO_FORMAT
    private val USA_FORMAT = DBHelper.USA_FORMAT
    lateinit var recyclerView: RecyclerView
    lateinit var dbHelper: DBHelper

    // TODOd #1: create the ItemHolder inner class
    // a holder object saves the references to view components of a recycler view item
    private inner class ItemHolder(view: View): RecyclerView.ViewHolder(view) {
        val itemID: TextView = view.findViewById(R.id.itemID)
        val itemStatus: ImageView= view.findViewById(R.id.itemStatus)
        val itemContent: TextView = view.findViewById(R.id.itemContent)
        val itemUpdated: TextView = view.findViewById(R.id.itemUpdated)
        val itemCreated: TextView = view.findViewById(R.id.itemCreated)
    }

    // TODOd #2: create the ItemAdapter inner class
    // an item adapter binds items from a list to holder objects in a recycler view
    private inner class ItemAdapter(var bucketlist: List<Item>, var onClickListener: View.OnClickListener, var onLongClickListener: View.OnLongClickListener): RecyclerView.Adapter<ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)

            // sets the holder's  on click listener for updating an item
            view.setOnClickListener(onClickListener)

            // sets the holder's on long click listener for the delete operation
            view.setOnLongClickListener(onLongClickListener)

            return ItemHolder(view)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = bucketlist[position]

            if (item.status==1 ) {
                 holder.itemStatus.setImageResource(R.drawable.completed_item)
            }
            else if (item.status == 2) {
                holder.itemStatus.setImageResource(R.drawable.archived_item)
            }
            else {
                holder.itemStatus.setImageResource(R.drawable.scheduled_item)
            }
            holder.itemContent.text = item.description
            holder.itemCreated.text = USA_FORMAT.format(item.creationDate)
            holder.itemUpdated.text = USA_FORMAT.format(item.updateDate)
            holder.itemID.text = item.id.toString()
        }

        override fun getItemCount(): Int {
            return bucketlist.size
        }
    }

    // TODOd #3: populate the recycler view
    // this function should query the database for all of the bucket list items; then use the list to update the recycler view's adapter
    // don't forget to call "sort()" on your list so the items are displayed in the correct order
    fun populateRecyclerView() {
        val db = dbHelper.readableDatabase
        val bucketlist = mutableListOf<Item>()
        val columns = arrayOf<String>("rowid, description, creation_date, update_date, status")
        val cursor = db.query(
            "bucketlist",
            columns, // maybe we need to add the select columns here? Tried to set a column array to the column names to use but got errors
            null,
            null,
            null,
            null,
            null
        )
        with (cursor) {
            while (moveToNext()) {
                val id = getInt(0)
                val description   = getString(1)
                val creationDate = ISO_FORMAT.parse(getString(2))
                val updateDate = ISO_FORMAT.parse(getString(3))
                val status     = getInt(4)
                val item = Item(id, description, creationDate, updateDate, status)
                bucketlist.add(item)

            }
        }

        bucketlist.sort()
        recyclerView.adapter = ItemAdapter(bucketlist, this, this)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODOd #4: create and populate the recycler view
        dbHelper = DBHelper(this)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        populateRecyclerView()

        // TODOd #5: initialize the floating action button
        val fabCreate: FloatingActionButton = findViewById(R.id.fabCreate)
        fabCreate.setOnClickListener {
            // calls CreateUpdateActivity for create
            val intent = Intent(this, CreateUpdateActivity::class.java)
            intent.putExtra("op", CreateUpdateActivity.CREATE_OP)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        populateRecyclerView()
    }

    // TODOd #6: call CreateUpdateActivity for update
    // don't forget to pass the item's id to the CreateUpdateActivity via the intent
    override fun onClick(view: View?) {
        if (view != null) {
            val rowid = view.findViewById<TextView>(R.id.itemID).text.toString().toInt()
            val intent = Intent(this, CreateUpdateActivity::class.java)
            intent.putExtra("op", CreateUpdateActivity.UPDATE_OP)
            intent.putExtra("rowid", rowid)

            startActivity(intent)
        }
    }

    // TODOd #7: delete the long tapped item after a yes/no confirmation dialog
    override fun onLongClick(view: View?): Boolean {

        class MyDialogInterfaceListener(val id: Int): DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    try {
                        val db = dbHelper.writableDatabase
                        db.execSQL("""
                            DELETE FROM bucketlist
                            WHERE rowid = $id
                        """)
                        populateRecyclerView()

                    } catch (ex: SQLiteException) {
                        Log.i("Error: ", ex.toString())
                    }
                }
            }
        }

        if (view != null) {
            val desc = view.findViewById<TextView>(R.id.itemContent).text.toString()
            val id = view.findViewById<TextView>(R.id.itemID).text.toString().toInt()
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Are you sure you want to delete:\n ${desc}\n from bucket list?")
            alertDialogBuilder.setPositiveButton("Yes", MyDialogInterfaceListener(id))
            alertDialogBuilder.setNegativeButton("No", MyDialogInterfaceListener(id))
            alertDialogBuilder.show()
            return true
        }
        return false
    }
}