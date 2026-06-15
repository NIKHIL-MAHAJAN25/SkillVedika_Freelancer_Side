package com.nikhil.sellerapp.Chatting


import android.graphics.drawable.Drawable
import android.util.Log

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController


import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.Utils.loge
import com.nikhil.sellerapp.Utils.snack
import com.nikhil.sellerapp.databinding.FragmentChatInterfaceBinding
import com.nikhil.sellerapp.databinding.FragmentSearchBinding
import com.nikhil.sellerapp.dataclasses.Chat
import com.nikhil.sellerapp.dataclasses.Message
import kotlin.text.clear

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatInterface.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatInterface : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentChatInterfaceBinding?=null
    lateinit var receiverUid:String
    val db= Firebase.firestore
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    lateinit var receiverName:String
    lateinit var chatAdapter: ChatAdapter
    lateinit var receiverImage:String
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            receiverUid = it.getString("receiverUid")!!
            receiverName = it.getString("receiverName")!!
            receiverImage = it.getString("receiverImage")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatInterfaceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loge("$receiverImage")
        val window = requireActivity().window

        // 1. Make status bar transparent so toolbar color shows through
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 2. White status bar icons (since dark green bg)
        // false means light icons (for dark backgrounds), true means dark icons (for light backgrounds)
        val windowController = WindowCompat.getInsetsController(window, view)
        windowController.isAppearanceLightStatusBars = false

        // 3. Pad the toolbar by status bar height so content doesn't go under icons
        ViewCompat.setOnApplyWindowInsetsListener(binding.topHeader) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            // Explicitly set bottom padding to 0 so we don't accidentally inherit extra space
            v.updatePadding(top = statusBarHeight, bottom = 0)
            insets
        }

        // Bottom input moves with keyboard
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomInputLayout) { v, windowInsets ->
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, maxOf(imeInsets.bottom, systemBars.bottom))
            windowInsets
        }
        setupinfo()
        setupRecycler()
        listenForMessages()
        binding.tvName.setOnClickListener {
            val bundle = Bundle().apply{
                putString("uid", receiverUid)

            }
            findNavController().navigate(
                R.id.ClientProfile,bundle
            )
        }
        binding.btnBack.setOnClickListener{
            findNavController().navigateUp()
        }
        binding.ivProfileImage.setOnClickListener {
            val bundle = Bundle().apply{
                putString("uid", receiverUid)

            }
            findNavController().navigate(
                R.id.ClientProfile,bundle
            )
        }

        binding.btnSend.setOnClickListener {
            if(!binding.etMessage.text.isNullOrBlank() || !binding.etMessage.text.trim().isEmpty()) {
                val text = binding.etMessage.text.trim().toString()
                sendMessage(text)

            }else{
                snack("Message is Empty")
            }
        }
    }
    private fun setupRecycler() {

        chatAdapter = ChatAdapter()

        binding.chatRecyclerView.apply {

            adapter = chatAdapter

            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext()).apply {

                stackFromEnd = true
            }
        }
    }
    private fun setupinfo()
    {
        binding.tvName.text=receiverName
        Glide.with(requireContext())
            .load(receiverImage)
            .centerCrop()
            .into(binding.ivProfileImage)
    }

    private fun listenForMessages() {

        val currentUid = auth.currentUser?.uid ?: return

        val chatId = if (currentUid < receiverUid) {
            "${currentUid}_${receiverUid}"
        } else {
            "${receiverUid}_${currentUid}"
        }

        db.collection("Chat")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->

                if (error != null) {

                    snack("Failed to load messages")
                    return@addSnapshotListener
                }

                val messages = value?.documents?.mapNotNull {

                    it.toObject(Message::class.java)

                } ?: emptyList()

                chatAdapter.submitList(messages)

                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }
    }
    private fun sendMessage(text: String)

    {
        val currentUid = auth.currentUser?.uid ?: return
        // deterministic chat id
        val chatId = if (currentUid < receiverUid) {
            "${currentUid}_${receiverUid}"
        } else {
            "${receiverUid}_${currentUid}"
        }
        val chatref = db.collection("Chat").document(chatId)
        val messageref=chatref.collection("messages").document()
        val message = Message(
            messageId = messageref.id,
            senderId = currentUid,
            text = text,
            timestamp = com.google.firebase.Timestamp.now()

        )
        val chat = Chat(
            chatId = chatId,
            participants = listOf(currentUid, receiverUid),
            lastMessage = text,
            lastMessageTime = Timestamp.now(),
            lastSenderId = currentUid,
            unreadCount = mapOf(
                currentUid to 0,
                receiverUid to 1
            )
        )
        chatref.set(chat).continueWithTask {
            messageref.set(message)
        }

            .addOnSuccessListener {

                binding.etMessage.text?.clear()

            }

            .addOnFailureListener {

                snack("Failed to send message")

            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatInterface.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatInterface().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}