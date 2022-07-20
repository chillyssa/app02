package edu.msudenver.bucketlist

/*
 * CS3013 - Mobile App Dev. - Summer 2022
 * Instructor: Thyago Mota
 * Student(s): Brea Chaney and Alyssa Williams
 * Description: App 02 - CreateUpdateActivity (controller) class
 */

import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class CreateUpdateActivity : AppCompatActivity(), View.OnClickListener {

    var op = CREATE_OP
    var id = 0
    lateinit var db: SQLiteDatabase
    lateinit var edtDescription: EditText
    lateinit var updateId: TextView
    private val ISO_FORMAT = DBHelper.ISO_FORMAT

    companion object {
        const val CREATE_OP = 0
        const val UPDATE_OP = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_update)

        //TODOd #8: get references to the view objects
        edtDescription = findViewById(R.id.editDesc)
        val spnStatus: Spinner = findViewById(R.id.statusCategory)
        updateId = findViewById(R.id.updateID)

        //TODOd #9: define the spinner's adapter as an ArrayAdapter of String
        spnStatus.adapter = ArrayAdapter<String>(this, R.layout.spinner_item, Item.STATUS_DESCRIPTIONS)

        // sets the "onItemSelectedListener" for the spinner, saving the status of the item when the spinner changes
        spnStatus.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, ud: Long) {
                id = position
            }
        }

        // TODOd #10: get a reference to the "CREATE/UPDATE" button and sets its listener
        val btnCreateUpdate: Button = findViewById(R.id.btnCreateUpdate)
        btnCreateUpdate.setOnClickListener(this)

        // TODOd #11: get a "writable" db connection
        val dbHelper = DBHelper(this)
        db = dbHelper.writableDatabase
        op = intent.getIntExtra("op", CREATE_OP)

        // TODOd #12d: set the button's text to "CREATE"; make sure the spinner's selection is Item.SCHEDULED and the spinner is not enabled
        if (op == CREATE_OP) {

            btnCreateUpdate.text = "CREATE"
            spnStatus.setSelection(Item.SCHEDULED)
            spnStatus.isEnabled = false
            spnStatus.isClickable = false
        }
        // TODOd #13: set the button's text to "UPDATE"; extract the item's id from the intent; use retrieveItem to retrieve the item's info; use the info to update the description and status view components
        else {
            btnCreateUpdate.text = "UPDATE"
            val id = intent.getIntExtra("rowid", -1)
            updateId.text = id.toString()
            updateId.isVisible = false //don't display rowid
            val item = retrieveItem(id)
            edtDescription.setText(item.description)
            spnStatus.setSelection(item.status)

        }
    }

    // TODO #14: return the item based on the given id
    // this function should query the database for the bucket list item identified by the given id; an item object should be returned
    fun retrieveItem(id: Int): Item {
        val columns = arrayOf<String>("rowid, description, creation_date, update_date, status")
        val cursor = db.query(
            "bucketlist",
            columns,
            "rowid = ${id}",
            null,
            null,
            null,
            null
        )
        with(cursor) {
            cursor.moveToNext()
            val id = getInt(0)
            val description = getString(1)
            val creationDate = ISO_FORMAT.parse(getString(2))
            val updateDate = ISO_FORMAT.parse(getString(3))
            val status = getInt(4)
            val item = Item(id, description, creationDate, updateDate, status)

            return item
        }

    }

    override fun onClick(view: View?) {

        // get a reference to the item's description
        val description = findViewById<EditText>(R.id.editDesc).text.toString()
        // both created_date and update_date should be set to current's date (use ISO format)
        val currentDate = ISO_FORMAT.format(Date())

        // TODOd #15: add a new item to the bucket list based on the information provided by the user
        if (op == CREATE_OP) {
            try {
                db.execSQL(
                    """
                    INSERT INTO bucketlist VALUES
                    ("${description}","${currentDate}","${currentDate}",${Item.SCHEDULED})
                    
                """
                )
                Toast.makeText(
                    this,
                    "New bucket list item created!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (ex: Exception) {
                print(ex.toString())
                Toast.makeText(this, "Exception when trying to create bucket list item", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        // TODOd #16: update the item identified by "id" - currently (7/17/22) the first 2 items update correctly. AW
        // update_date should be set to current's date (use ISO format)
        else {
            val rowid = updateId.text.toString().toInt()
            try {
                db.execSQL(
                    """
                UPDATE bucketlist SET
                    description = "${description}",
                    update_date = "${currentDate}", 
                    status = ${id} 
                    WHERE rowid = $rowid
            """
                )
                Toast.makeText(
                    this,
                    "Bucket list item successfully updated!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (ex: Exception) {
                print(ex.toString())
                Toast.makeText(
                    this,
                    "Exception when trying to update bucket list item.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        finish()
    }
}