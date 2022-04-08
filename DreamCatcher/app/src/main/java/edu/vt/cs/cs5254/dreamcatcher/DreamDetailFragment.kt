package edu.vt.cs.cs5254.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Index
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import edu.vt.cs.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import edu.vt.cs.cs5254.dreamcatcher.databinding.ListItemDreamEntryBinding
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.UUID

private const val TAG = "DreamDetailFragment"
private const val ARG_DREAM_ID = "dream_id"
private const val REQUEST_KEY_ADD_REFLECTION = "add_reflection"
private const val BUNDLE_KEY_REFLECTION_TEXT = "reflection_text"


class DreamDetailFragment : Fragment() {

    private val fulfilledButtonColor="#16B896"
    private val conceivedButtonColor="#FFB6C1"
    private val reflectionButtonColor="#7393B3"
    private val deferredButtonColor="#FF4433"

    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }
    private var callbacks: Callbacks? = null
    private lateinit var titleField: EditText
    private lateinit var dreamWithEntries: DreamWithEntries
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var photoLauncher: ActivityResultLauncher<Uri>

    private val vm: DreamDetailViewModel by lazy {
        ViewModelProvider(this).get(DreamDetailViewModel::class.java)
    }

    private var _binding: FragmentDreamDetailBinding?=null
    private val binding get()=_binding!!

    private var adapter: DreamEntryAdapter? = DreamEntryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
        dreamWithEntries = DreamWithEntries(Dream(), emptyList())
        val dreamId: UUID=arguments?.getSerializable(ARG_DREAM_ID) as UUID
        vm.loadDream(dreamId)
        setHasOptionsMenu(true)
        photoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                updatePhotoView()
            }
            requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentDreamDetailBinding.inflate(inflater, container, false)
        val view=binding.root
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        itemTouchHelper.attachToRecyclerView(binding.dreamEntryRecyclerView)

        titleField=view.findViewById(R.id.dream_title_text) as EditText
        binding.dreamEntryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.dreamEntryRecyclerView.adapter = adapter

        binding.dreamTitleText.setText(dreamWithEntries.dream.title)

        Log.d(TAG, "onCreateView() called")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.dreamLiveData.observe(
            viewLifecycleOwner,
            Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = it
                    photoFile = vm.getPhotoFile(dreamWithEntries)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "edu.vt.cs.cs5254.dreamcatcher.fileprovider",
                        photoFile)
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
        val titleWatcher=object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                dreamWithEntries.dream.title=sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        binding.dreamDeferredCheckbox.setOnClickListener { onDeferredClick() }
        binding.dreamFulfilledCheckbox.setOnClickListener { onFulfilledClick() }
        binding.addReflectionButton.setOnClickListener {
            val addReflection: DialogFragment = AddReflectionDialog()
            addReflection.show(parentFragmentManager, REQUEST_KEY_ADD_REFLECTION)
        }
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_ADD_REFLECTION,
            viewLifecycleOwner)
        { _, bundle ->
            var reflectionText = bundle.getString(BUNDLE_KEY_REFLECTION_TEXT, "")
            dreamWithEntries.dreamEntries += DreamEntry(
                dreamId = dreamWithEntries.dream.id,
                text = reflectionText,
                kind = DreamEntryKind.REFLECTION
            )
            updateUI()
        }
    }

    override fun onStop() {
        super.onStop()
        vm.saveDream()
    }

    private fun onFulfilledClick() {
        if (binding.dreamFulfilledCheckbox.isChecked) {
            dreamWithEntries.dream.isFulfilled = true
            binding.dreamDeferredCheckbox.isEnabled = false
            binding.addReflectionButton.isEnabled = false
            val newDreamEntry = DreamEntry(id = UUID.randomUUID(),
                date = Date(),
                text = "",
                kind = DreamEntryKind.FULFILLED,
                dreamId = dreamWithEntries.dream.id)
            dreamWithEntries.dreamEntries += newDreamEntry
        } else {
            dreamWithEntries.dream.isFulfilled = false
            binding.dreamDeferredCheckbox.isEnabled = true
            binding.addReflectionButton.isEnabled = true
            dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries.dropLast(1)
        }
        updateUI()
    }

    private fun onDeferredClick() {
        if (binding.dreamDeferredCheckbox.isChecked) {
            dreamWithEntries.dream.isDeferred = true
            binding.dreamFulfilledCheckbox.isEnabled = false
            val newDreamEntry=DreamEntry(id = UUID.randomUUID(),
                date = Date(),
                text = "",
                kind = DreamEntryKind.DEFERRED,
                dreamId = dreamWithEntries.dream.id)
            dreamWithEntries.dreamEntries += newDreamEntry
        } else {
            dreamWithEntries.dream.isDeferred = false
            binding.dreamFulfilledCheckbox.isEnabled = true
            dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries.filterNot {
                it.kind == DreamEntryKind.DEFERRED
            }
        }
        updateUI()
    }

    companion object {
        fun newInstance(dreamId: UUID): DreamDetailFragment {
            val args=Bundle().apply {
                putSerializable(ARG_DREAM_ID, dreamId)
            }
            return DreamDetailFragment().apply {
                arguments=args
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        _binding=null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_detail, menu)
        val cameraAvailable = PictureUtils.isCameraAvailable(requireActivity())
        val menuItem = menu.findItem(R.id.take_dream_photo)
        menuItem.apply {
            Log.d(TAG, "Camera available: $cameraAvailable")
            isEnabled = cameraAvailable
            isVisible = cameraAvailable
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.take_dream_photo -> {
                val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                }
                requireActivity().packageManager
                    .queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    .forEach { cameraActivity ->
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                photoLauncher.launch(photoUri)
                true
            }
            R.id.share_dream -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getDreamReport())
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        dreamWithEntries.dream.title
                    ).also { intent ->
                        val chooserIntent =
                            Intent.createChooser(intent, getString(R.string.send_report))
                        startActivity(chooserIntent)
                    }
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setButtonColor(button: Button, colorString: String) {
        button.backgroundTintList=
            ColorStateList.valueOf(Color.parseColor(colorString))
        button.setTextColor(Color.WHITE)
        button.alpha=1f
    }

    private fun updateUI() {
        Log.d(TAG, "updateUI() called")
        binding.dreamTitleText.setText(dreamWithEntries.dream.title)
        when {
            dreamWithEntries.dream.isFulfilled -> {
                binding.dreamFulfilledCheckbox.isChecked = true
                binding.dreamDeferredCheckbox.isEnabled = false
                binding.addReflectionButton.isEnabled = false
            }
            dreamWithEntries.dream.isDeferred -> {
                binding.dreamDeferredCheckbox.isChecked = true
                binding.dreamFulfilledCheckbox.isEnabled = false

            }
            else -> {
                binding.dreamFulfilledCheckbox.isEnabled = true
                binding.dreamDeferredCheckbox.isEnabled = true
            }

        }
        updatePhotoView()
        updateButtons()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile.path, 120, 120)
            binding.dreamPhoto.setImageBitmap(bitmap)
        } else {
            binding.dreamPhoto.setImageDrawable(null)
        }
    }

    private fun updateButtons() {
        adapter = DreamEntryAdapter()
        binding.dreamEntryRecyclerView.adapter = adapter
    }


    inner class DreamEntryHolder(private val itemBinding: ListItemDreamEntryBinding)
        : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        private lateinit var dreamEntry: DreamEntry
        private val df = DateFormat.getDateInstance(DateFormat.MEDIUM)
        private val buttonView: Button = itemBinding.dreamEntryButton

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(dreamEntry: DreamEntry) {
            this.dreamEntry = dreamEntry
            when (dreamEntry.kind) {
                DreamEntryKind.FULFILLED -> {
                    setButtonColor(buttonView, fulfilledButtonColor)
                    buttonView.text = dreamEntry.kind.toString()
                    buttonView.visibility = View.VISIBLE
                }
                DreamEntryKind.DEFERRED -> {
                    setButtonColor(buttonView, deferredButtonColor)
                    buttonView.text = dreamEntry.kind.toString()
                    buttonView.visibility = View.VISIBLE
                }
                DreamEntryKind.CONCEIVED -> {
                    setButtonColor(buttonView, conceivedButtonColor)
                    buttonView.text = dreamEntry.kind.toString()
                    buttonView.visibility = View.VISIBLE
                }
                DreamEntryKind.REFLECTION -> {
                    setButtonColor(buttonView, reflectionButtonColor)
                    val df = DateFormat.getDateInstance(DateFormat.MEDIUM)
                    val formattedDate = df.format(dreamEntry.date)
                    buttonView.text = formattedDate.toString() + ": " + dreamEntry.text
                    buttonView.visibility = View.VISIBLE

                }
            }
        }

        override fun onClick(v: View) {
            callbacks?.onDreamSelected(dreamEntry.id)
        }
    }

    inner class DreamEntryAdapter()
        : RecyclerView.Adapter<DreamEntryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : DreamEntryHolder {
            val itemBinding = ListItemDreamEntryBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return DreamEntryHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
            val dreamEntry = dreamWithEntries.dreamEntries[position]
            holder.bind(dreamEntry)
        }
        override fun getItemCount() = dreamWithEntries.dreamEntries.size

        fun deleteItem(position: Int) {
            val dreamToDelete = dreamWithEntries.dreamEntries[position]
            Log.d("test", "${dreamWithEntries.dreamEntries.size}")
            if (dreamToDelete.kind == DreamEntryKind.REFLECTION) {
                dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries - dreamToDelete
                vm.refreshDreamEntries(dreamWithEntries.dreamEntries)
                notifyItemRemoved(position)
            } else {
                notifyItemChanged(position)
            }
        }
    }

    inner class SwipeToDeleteCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter?.deleteItem(position)
        }
    }

    private fun getDreamReport(): String {
        val dreamStatusString = when {
            dreamWithEntries.dream.isFulfilled -> {
                getString(R.string.dream_report_fulfilled)
            }
            dreamWithEntries.dream.isDeferred -> {
                getString(R.string.dream_report_deferred)
            }
            else -> {
                getString(R.string.dream_report_undecided)
            }
        }
        val df = DateFormat.getDateInstance(DateFormat.MEDIUM)
        var refTxt = getString(R.string.dream_report_no_reflection)
        if (dreamWithEntries.dreamEntries.any {
            it.kind == DreamEntryKind.REFLECTION
            }) {
            refTxt = "Dream Reflections: " + "\n"
            dreamWithEntries.dreamEntries.forEach {
                if (it.kind == DreamEntryKind.REFLECTION) {
                    refTxt += "- " + it.text + "\n"
                }
            }
        }
        return getString(R.string.dream_report,
            ">> " + dreamWithEntries.dream.title + " <<",
            df.format(dreamWithEntries.dream.date), refTxt, dreamStatusString)
    }

}