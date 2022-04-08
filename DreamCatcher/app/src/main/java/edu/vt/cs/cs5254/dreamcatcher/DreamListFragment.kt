package edu.vt.cs.cs5254.dreamcatcher

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamListBinding
import edu.vt.cs.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import java.text.DateFormat
import java.util.UUID

private const val TAG = "DreamListFragment"

class DreamListFragment : Fragment() {
    /**
     * Required interface for hosting activities
     */
    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }
    private var callbacks: Callbacks? = null
    private var _binding: FragmentDreamListBinding? = null
    private val binding get() = _binding!!
    private var adapter: DreamAdapter? = DreamAdapter(emptyList())
    private val vm: DreamListViewModel by lazy {
        ViewModelProvider(this).get(DreamListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    companion object {
        fun newInstance(): DreamListFragment {
            return DreamListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.dreamRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.dreamListLiveData.observe(
            viewLifecycleOwner,
            Observer { dreams ->
                dreams?.let {
                    Log.i(TAG, "Got dreams ${dreams.size}")
                    updateUI(dreams)
                }
            })
    }

    private fun updateUI(dreams: List<Dream>) {
        adapter = DreamAdapter(dreams)
        binding.dreamRecyclerView.adapter = adapter
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_list, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_dream -> {
                val dreamWithEntries = DreamWithEntries(Dream(), emptyList())
                dreamWithEntries.dreamEntries += DreamEntry(
                    dreamId = dreamWithEntries.dream.id,
                    kind = DreamEntryKind.CONCEIVED
                )
                vm.addDreamWithEntries(dreamWithEntries)
                callbacks?.onDreamSelected(dreamWithEntries.dream.id)
                true
            }
            R.id.delete_all_dreams -> {
                vm.deleteAllDreams()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    inner class DreamHolder(private val itemBinding: ListItemDreamBinding)
        : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        private lateinit var dream: Dream

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(dream: Dream) {
            this.dream = dream
            itemBinding.dreamItemTitle.text = this.dream.title
            itemBinding.dreamItemDate.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(this.dream.date)
            when {
                dream.isDeferred -> {
                    itemBinding.dreamItemImage.setImageResource(R.drawable.dream_deferred_icon)
                    itemBinding.dreamItemImage.tag = R.drawable.dream_deferred_icon
                }
                dream.isFulfilled -> {
                    itemBinding.dreamItemImage.setImageResource(R.drawable.dream_fulfilled_icon)
                    itemBinding.dreamItemImage.tag = R.drawable.dream_fulfilled_icon
                }
                else -> {
                    itemBinding.dreamItemImage.setImageResource(0)
                    itemBinding.dreamItemImage.tag = 0
                }
            }
        }

        override fun onClick(v: View) {
            callbacks?.onDreamSelected(dream.id)
        }
    }

    inner class DreamAdapter(var dreams: List<Dream>)
        : RecyclerView.Adapter<DreamHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : DreamHolder {
            val itemBinding = ListItemDreamBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return DreamHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: DreamHolder, position: Int) {
            val dream = dreams[position]
            holder.bind(dream)
        }
        override fun getItemCount() = dreams.size
    }

}