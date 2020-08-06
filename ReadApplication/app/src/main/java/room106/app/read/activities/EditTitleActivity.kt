package room106.app.read.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import room106.app.read.R

class EditTitleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_title)

        // TODO - Add "Done" button for EditText while multiline is available using:
//        setImeOptions(EditorInfo.IME_ACTION_DONE);
//        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }
}