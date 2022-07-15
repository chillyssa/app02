package edu.msudenver.bucketlist

/*
 * CS3013 - Mobile App Dev. - Summer 2022
 * Instructor: Thyago Mota
 * Student(s): Brea Chaney and Alyssa Williams
 * Description: App 02 - CreateUpdateActivity (controller) class
 */

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import java.util.*

class CreateUpdateActivity : AppCompatActivity(), View.OnClickListener {

    var op = CREATE_OP
    var id = 0
    lateinit var db: SQLiteDatabase
    lateinit var edtDescription: EditText
    lateinit var spnStatus: Spinner
    private val ISO_FORMAT = DBHelper.ISO_FORMAT
    private val USA_FORMAT = DBHelper.USA_FORMAT

    companion object {
        const val CREATE_OP = 0
        const val UPDATE_OP = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_update)

         //TODOd #8: get references to the view objects
        edtDescription=findViewById(R.id.editDesc)
        spnStatus= findViewById(R.id.statusCategory)
        
         //TODOd #9: define the spinner's adapter as an ArrayAdapter of String
        spnStatus.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)

        // TODOd #10: get a reference to the "CREATE/UPDATE" button and sets its listener
        val btnCreateUpdate:Button=findViewById(R.id.btnCreateUpdate)
        btnCreateUpdate.setOnClickListener (this)

        // TODOd #11: get a "writable" db connection
        val dbHelper = DBHelper(this)
        db = dbHelper.writableDatabase
        op = intent.getIntExtra("op", CREATE_OP)

        // TODOd #12d: set the button's text to "CREATE"; make sure the spinner's selection is Item.SCHEDULED and the spinner is not enabled
        if (op == CREATE_OP) {
            btnCreateUpdate.text="CREATE"
            spnStatus.setSelection(Item.SCHEDULED)
            spnStatus.isEnabled= false
        }
        // TODO #13: set the button's text to "UPDATE"; extract the item's id from the intent; use retrieveItem to retrieve the item's info; use the info to update the description and status view components
        else {
            btnCreateUpdate.text="UPDATE"
            val id=intent.getIntExtra("id", id)
            retrieveItem(id)
            //edtDescription.text= <- TODO get info from retrieve item.
            //spnStatus.setSelection( <-TODO get selection)

            
        }
    }

    // TODO #14: return the item based on the given id
    // this function should query the database for the bucket list item identified by the given id; an item object should be returned
    fun retrieveItem(id: Int): Item {
        val cursor = db.query(
            "bucketlist",
            null,
            "id = \"${id}\"",
            null,
            null,
            null,
            null
        )
        with (cursor) {
            cursor.moveToNext()
            val id = cursor.hashCode() // <-TODO verify this is the right id
            val description   = getString(1)
            val creationDate = ISO_FORMAT.parse(getString(2))
            val updateDate = ISO_FORMAT.parse(getString(3))
            val status     = getInt(4)
            val item = Item(id, description, creationDate, updateDate, status)
            return item
        }

    }

    override fun onClick(view: View?) {
        
        // TODO #15: add a new item to the bucket list based on the information provided by the user
        // both created_date and update_date should be set to current's date (use ISO format)
        // status should be set to Item.SCHEDULED
        if (op == CREATE_OP) {
            
        }
        // TODO #16: update the item identified by "id"
        // update_date should be set to current's date (use ISO format)
        else {
            
        }
        finish()
    }
}