package com.example.game.model

enum class MissionGoalType {
  REACH_TARGET,
  DELIVERY_RUN,
  VEHICLE_RACE,
  PROP_SMASH,
  CHASE_CAPTURE,
  INTERACT_FIX,
}

data class MissionDef(
  val id: String,
  val number: Int,
  val titleEn: String,
  val titleBn: String,
  val descEn: String,
  val descBn: String,
  val funnyDialogueEn: String,
  val funnyDialogueBn: String,
  val district: DistrictId,
  val goalType: MissionGoalType,
  val startX: Float,
  val startZ: Float,
  val targetX: Float,
  val targetZ: Float,
  val rewardTaka: Long,
  val timeLimitSec: Int,
  val targetCount: Int = 1,
)

object MissionCatalog {
  val missions: List<MissionDef> = listOf(
    MissionDef(
      "m01_ips_war", 1, "IPS War", "আইপিএস যুদ্ধ",
      "Power went out! Race to the DESCO fuse box in Old Dhaka and flip power back before the neighbor yells.",
      "কারেন্ট চলে গেছে! রাগী আঙ্কেল চিল্লানোর আগেই পুরান ঢাকার ইলেকট্রিক বক্স ঠিক করো।",
      "Neighbor Uncle: 'In my youth we studied under streetlamps! Fix that IPS!'",
      "রাগী আঙ্কেল: 'কারেন্ট গেল কেন? তাড়াতাড়ি আইপিএস চালু কর!'",
      DistrictId.OLD_DHAKA, MissionGoalType.INTERACT_FIX, 12f, 12f, -18f, 24f, 350L, 90
    ),
    MissionDef(
      "m02_tea_debt", 2, "Tea Shop Debt", "চায়ের দোকানের বাকি",
      "Cha Mama wants his ৳120 tab! Grab parcels or reach the Commercial Hub to earn and settle up.",
      "চা মামা বাকি টাকা চাচ্ছে! বাণিজ্যিক এলাকায় গিয়ে ডেলিভারি শেষ করে টাকা জোগাড় করো।",
      "Cha Mama: '18 cups of malai cha on credit? My notebook is full!'",
      "চা মামা: 'আঠারো কাপ মালাই চা বাকি! খাতা ভরে গেছে রে!'",
      DistrictId.OLD_DHAKA, MissionGoalType.DELIVERY_RUN, -22f, -8f, 125f, -45f, 400L, 120
    ),
    MissionDef(
      "m03_exam_hall", 3, "Exam Hall Chaos", "পরীক্ষার হলে ঝামেলা",
      "Deliver forgotten admit card from Residential Area to Modern Dhaka University gate!",
      "আবাসিক এলাকা থেকে অ্যাডমিট কার্ড নিয়ে নতুন ঢাকার গেটে পৌঁছে দাও!",
      "Student: 'Bro I forgot my pen AND admit card! Drive faster!'",
      "ছাত্র: 'ভাই অ্যাডমিট কার্ড বাসায় ফেলে আসছি! জোরে চালাও!'",
      DistrictId.RESIDENTIAL, MissionGoalType.DELIVERY_RUN, -130f, 10f, 0f, -125f, 500L, 100
    ),
    MissionDef(
      "m04_wedding_food", 4, "Wedding Food War", "বিয়ে বাড়ির দাওয়াত",
      "Reach the Commercial banquet hall before the Kacchi pot runs empty!",
      "কাচ্চির হাঁড়ি শেষ হওয়ার আগেই বাণিজ্যিক এলাকার কমিউনিটি সেন্টারে পৌঁছো!",
      "Uncle: 'If you miss the borhani & roast, don't blame me!'",
      "মামা: 'দেরি করলে রোস্ট আর বোরহানি কিচ্ছু পাবি না!'",
      DistrictId.COMMERCIAL, MissionGoalType.REACH_TARGET, 35f, -10f, 145f, -55f, 450L, 85
    ),
    MissionDef(
      "m05_rickshaw_race", 5, "Rickshaw Race", "রিকশা রেস",
      "Hop on a Rickshaw and sprint across Old Dhaka to Modern Dhaka flyover!",
      "রিকশা নিয়ে পুরান ঢাকা থেকে নতুন ঢাকার ফ্লাইওভারে সবার আগে পৌঁছো!",
      "Rickshaw Puller: 'Hold onto the hood! Full pedal power!'",
      "রিকশাওয়ালা: 'হুড ধরে বসো মামা! আজ রিকশা উড়বে!'",
      DistrictId.OLD_DHAKA, MissionGoalType.VEHICLE_RACE, 0f, 18f, 10f, -130f, 600L, 95
    ),
    MissionDef(
      "m06_mosquito", 6, "Mosquito Nightmare", "মশার উৎপাত",
      "Smash 4 old stagnant water pots & buckets around Old Dhaka alleyways!",
      "পুরান ঢাকার গলির ৪টি পুরোনো মাটির কলসি ও জমে থাকা পাত্র ভেঙে ফেলো!",
      "Pedestrian: 'These Dhaka mosquitoes carry tiny passports!'",
      "পথচারী: 'মশার সাইজ দেখছো? হেলিকপ্টারের মতো উড়ছে!'",
      DistrictId.OLD_DHAKA, MissionGoalType.PROP_SMASH, -10f, 20f, 15f, 30f, 380L, 110, 4
    ),
    MissionDef(
      "m07_rooftop_cricket", 7, "Rooftop Cricket", "ছাদের ক্রিকেট",
      "Climb the Residential stairs/ladder to the rooftop pitch and hit a huge sixer!",
      "আবাসিক এলাকার ছাদে উঠে গলি ক্রিকেটে বিশাল ছক্কা মারো!",
      "Kids: 'If it falls on Angry Uncle's balcony, YOU get the ball!'",
      "বাচ্চারা: 'বল আঙ্কেলের বারান্দায় পড়লে কিন্তু তুমি আনবা!'",
      DistrictId.RESIDENTIAL, MissionGoalType.INTERACT_FIX, -128f, 12f, -140f, -10f, 420L, 100
    ),
    MissionDef(
      "m08_waterlogged", 8, "Waterlogged Dhaka", "জলমগ্ন ঢাকা",
      "Navigate a CNG or Boat from Modern Dhaka down to Buriganga Ghat before floodwaters peak!",
      "নতুন ঢাকা থেকে সিএনজি বা নৌকায় বুড়িগঙ্গা ঘাটে পৌঁছে যাও!",
      "Vendor: 'Free swimming pool on Mirpur Road today!'",
      "হকার: 'রাস্তায় হাঁটু পানি! আজ নৌকা চলবে!'",
      DistrictId.MODERN_DHAKA, MissionGoalType.REACH_TARGET, 8f, -125f, 0f, 130f, 550L, 110
    ),
    MissionDef(
      "m09_internet_disaster", 9, "Internet Disaster", "ওয়াইফাই বিপর্যয়",
      "Reset the Commercial ISP router tower to restore neighborhood broadband!",
      "পাড়ার ওয়াইফাই ঠিক করতে বাণিজ্যিক এলাকার আইএসপি বক্স রিসেট করো!",
      "Gamer: '999ms ping during ranked match! Save us!'",
      "গেমার: 'ভাই ৯৯৯ পিং! তাড়াতাড়ি ওয়াইফাই ঠিক করো!'",
      DistrictId.COMMERCIAL, MissionGoalType.INTERACT_FIX, 120f, -35f, 152f, -48f, 460L, 90
    ),
    MissionDef(
      "m10_cow_road", 10, "Cow on the Road", "রাস্তায় গরু",
      "A stubborn cow is blocking Village Road! Reach it and shoo/nudge traffic clear.",
      "গ্রামের রাস্তার মাঝে গরু বসে পড়েছে! গিয়ে রাস্তা ক্লিয়ার করো।",
      "Driver: 'Honking doesn't move a VIP cow, bro!'",
      "ড্রাইভার: 'হর্ন দিলেও গরু নড়ছে না মামা!'",
      DistrictId.VILLAGE, MissionGoalType.CHASE_CAPTURE, -110f, -120f, -142f, -135f, 360L, 95
    ),
    MissionDef(
      "m11_market_bargain", 11, "Market Bargaining", "বাজারে দরদাম",
      "Visit the Village Haat and Old Dhaka market stall to score discount groceries!",
      "গ্রামের হাট আর পুরান ঢাকার দোকানে গিয়ে দরদাম করে বাজার করো!",
      "Shopkeeper: 'This Hilsha went to college! Final price ৳600!'",
      "দোকানদার: 'একদম তাজা মাছ! এর নিচে দেওয়া যাবে না!'",
      DistrictId.VILLAGE, MissionGoalType.INTERACT_FIX, -125f, -140f, -132f, -125f, 340L, 100
    ),
    MissionDef(
      "m12_bus_counter", 12, "Bus Counter Madness", "বাস কাউন্টার পাগলামি",
      "Sprint to the Commercial Bus Terminal before the express coach leaves!",
      "বাস ছেড়ে দেওয়ার আগেই বাণিজ্যিক বাস টার্মিনালে পৌঁছে যাও!",
      "Helper: 'Last seat! Chittagong-Cox's Bazar, hurry up!'",
      "হেলপার: 'লাস্ট সিট খালি আছে! ওস্তাদ টান দেন!'",
      DistrictId.COMMERCIAL, MissionGoalType.REACH_TARGET, 115f, -20f, 150f, -70f, 480L, 85
    ),
    MissionDef(
      "m13_lost_phone", 13, "Lost Mobile Phone", "হারানো মোবাইল",
      "Track down your dropped phone near the Buriganga riverside market!",
      "বুড়িগঙ্গা ঘাটের বাজারের পাশে হারিয়ে যাওয়া মোবাইল খুঁজে বের করো!",
      "Friend: 'Did you leave it at the fuchka cart again?!'",
      "বন্ধু: 'আবার কি ফুচকার দোকানে ফোন ফেলে আসলি?!'",
      DistrictId.RIVERSIDE, MissionGoalType.REACH_TARGET, -15f, 115f, 22f, 132f, 430L, 90
    ),
    MissionDef(
      "m14_biryani_delivery", 14, "Biryani Delivery", "কাচ্চি ডেলিভারি",
      "Hot Kacchi from Old Dhaka to Modern Dhaka office tower—don't let it get cold!",
      "পুরান ঢাকা থেকে গরম কাচ্চি নিয়ে নতুন ঢাকার অফিসে পৌঁছে দাও!",
      "Customer: 'Extra alu is mandatory or zero stars!'",
      "কাস্টমার: 'আলু আর বোরহানি না থাকলে কিন্তু ওয়ান স্টার!'",
      DistrictId.OLD_DHAKA, MissionGoalType.DELIVERY_RUN, 5f, 8f, -12f, -142f, 650L, 100
    ),
    MissionDef(
      "m15_angry_uncle", 15, "Escaping Angry Uncle", "রাগী আঙ্কেল থেকে পলায়ন",
      "You broke a flowerpot! Sprint out of Residential Area to Riverside!",
      "আঙ্কেলের ফুলের টব ভেঙেছে! আবাসিক এলাকা থেকে বুড়িগঙ্গা ঘাটে পালাও!",
      "Uncle: 'Come back here! I know your father!'",
      "আঙ্কেল: 'দাঁড়া হতভাগা! তোর বাবারে বলে দিচ্ছি!'",
      DistrictId.RESIDENTIAL, MissionGoalType.REACH_TARGET, -135f, 10f, -10f, 128f, 520L, 90
    ),
    MissionDef(
      "m16_missing_goat", 16, "The Missing Goat", "হারানো ছাগল",
      "Chase down the runaway goat in the Village fields before it eats the laundry!",
      "গ্রামের মাঠ থেকে পলাতক ছাগলটাকে ধরে ফেলো!",
      "Villager: 'That goat ate two lungis already!'",
      "গ্রামবাসী: 'ছাগলটা দুইটা লুঙ্গি চিবিয়ে খেয়ে ফেললো!'",
      DistrictId.VILLAGE, MissionGoalType.CHASE_CAPTURE, -140f, -120f, -155f, -148f, 390L, 90
    ),
    MissionDef(
      "m17_broken_pipe", 17, "Broken Water Pipe", "ভাঙা পানির পাইপ",
      "Shut off the gushing WASA tap in Residential lane before the street floods!",
      "আবাসিক গলির ভাঙা পানির কলটা গিয়ে বন্ধ করো!",
      "Neighbor: 'The whole lane turned into Cox's Bazar!'",
      "প্রতিবেশী: 'গলি তো কক্সবাজার সমুদ্র সৈকত হয়ে গেল!'",
      DistrictId.RESIDENTIAL, MissionGoalType.INTERACT_FIX, -120f, -15f, -148f, 8f, 370L, 85
    ),
    MissionDef(
      "m18_electric_repair", 18, "Electricity Repair", "কারেন্ট মেরামত",
      "Inspect the Modern Dhaka transformer box and restore plaza lights!",
      "নতুন ঢাকার ট্রান্সফরমার বক্স ঠিক করে আলো ফিরিয়ে আনো!",
      "Guard: 'The mall escalator stopped halfway!'",
      "গার্ড: 'মার্কেটের চলন্ত সিঁড়ি মাঝপথে আটকে গেছে!'",
      DistrictId.MODERN_DHAKA, MissionGoalType.INTERACT_FIX, 15f, -120f, -20f, -145f, 510L, 90
    ),
    MissionDef(
      "m19_last_bus", 19, "The Last Bus", "শেষ বাস",
      "Catch the Local Bus at Commercial Hub and drive it to Old Dhaka Chowk!",
      "বাণিজ্যিক এলাকা থেকে লোকাল বাস নিয়ে পুরান ঢাকায় চলে এসো!",
      "Conductor: 'Move inside, plenty of room on the roof!'",
      "কন্ডাক্টর: 'ভিতরে চাপেন ভাই, ছাদে অনেক জায়গা!'",
      DistrictId.COMMERCIAL, MissionGoalType.VEHICLE_RACE, 140f, -50f, 5f, 5f, 620L, 110
    ),
    MissionDef(
      "m20_chicken_chase", 20, "Chicken Chase", "মুরগি ধরা",
      "Catch the escaped market chicken running wild in Commercial Hub!",
      "বাণিজ্যিক বাজারে ছুটে বেড়ানো মুরগিটাকে ধরে ফেলো!",
      "Vendor: 'Catch that deshi murgi before it enters the bank!'",
      "বিক্রেতা: 'দেশি মুরগি ব্যাংকের ভিতর ঢুকে পড়লো রে!'",
      DistrictId.COMMERCIAL, MissionGoalType.CHASE_CAPTURE, 128f, -38f, 145f, -28f, 360L, 85
    ),
    MissionDef(
      "m21_grocery", 21, "Grocery Shopping", "মুদি বাজার",
      "Pick up rice, onions, and oil from Mudi Dokan and carry them back to Home!",
      "মুদি দোকান থেকে চাল-পেঁয়াজ নিয়ে আবাসিক এলাকার বাসায় ফিরে এসো!",
      "Mom: 'Don't forget the coriander or stay outside!'",
      "আম্মু: 'ধনেপাতা আনতে ভুললে বাসায় ঢুকবি না!'",
      DistrictId.OLD_DHAKA, MissionGoalType.DELIVERY_RUN, -15f, 10f, -135f, 15f, 480L, 120
    ),
    MissionDef(
      "m22_rainy_delivery", 22, "Rainy Day Delivery", "বৃষ্টির দিনে ডেলিভারি",
      "Monsoon rain hits! Deliver an urgent parcel from Modern Dhaka to Village Area.",
      "ঝুম বৃষ্টির মধ্যে নতুন ঢাকা থেকে গ্রামে জরুরি পার্সেল পৌঁছে দাও!",
      "Rider: 'Slippery mud roads ahead—drive carefully!'",
      "রাইডার: 'রাস্তা কিন্তু খুব পিচ্ছিল—সাবধানে চালাও!'",
      DistrictId.MODERN_DHAKA, MissionGoalType.DELIVERY_RUN, 0f, -135f, -135f, -135f, 700L, 130
    ),
    MissionDef(
      "m23_stolen_football", 23, "The Stolen Football", "চুরি হওয়া ফুটবল",
      "A street dog ran off with the neighborhood football! Track it down in the park.",
      "পাড়ার ফুটবল কুকুর নিয়ে পালালো! পার্ক থেকে বল উদ্ধার করো।",
      "Kid: 'Brazil vs Argentina final is paused because of Tommy!'",
      "বাচ্চা: 'টমি বল নিয়ে যাওয়ায় ব্রাজিল-আর্জেন্টিনা ম্যাচ আটকে আছে!'",
      DistrictId.RESIDENTIAL, MissionGoalType.CHASE_CAPTURE, -140f, 18f, -122f, -22f, 410L, 90
    ),
    MissionDef(
      "m24_wedding_card", 24, "Wedding Card Disaster", "বিয়ের কার্ড বিপর্যয়",
      "Distribute the last wedding invitation from Old Dhaka to Riverside Ghat!",
      "পুরান ঢাকা থেকে বুড়িগঙ্গা ঘাটে বিয়ের শেষ কার্ডটা পৌঁছে দাও!",
      "Cousin: 'If Chacha doesn't get a card, wedding is cancelled!'",
      "ভাই: 'চাচা কার্ড না পেলে কিন্তু বিয়ে বাড়িতে তুলকালাম হবে!'",
      DistrictId.OLD_DHAKA, MissionGoalType.DELIVERY_RUN, 10f, -10f, 15f, 130f, 470L, 100
    ),
    MissionDef(
      "m25_water_tank", 25, "Rooftop Water Tank", "ছাদের পানির ট্যাংক",
      "Climb the Residential building ladder and fix the overflowing rooftop tank!",
      "ছাদের মই বেয়ে উঠে পানির ট্যাংকের ভাল্ব বন্ধ করো!",
      "Caretaker: 'The Gazi tank is overflowing since morning!'",
      "দারোয়ান: 'সকাল থেকে গাজী ট্যাংক উপচে পড়ছে!'",
      DistrictId.RESIDENTIAL, MissionGoalType.INTERACT_FIX, -135f, -5f, -142f, 2f, 440L, 95
    ),
    MissionDef(
      "m26_cng_fare", 26, "CNG Fare Negotiation", "সিএনজি ভাড়া দরকষাকষি",
      "Drive a CNG from Modern Dhaka to Commercial Hub faster than the meter!",
      "মিটারের চেয়ে দ্রুত সিএনজি চালিয়ে বাণিজ্যিক এলাকায় পৌঁছো!",
      "CNG Mama: 'I don't go by meter, only vibes and ৳300!'",
      "সিএনজি মামা: 'মিটারে যাবো না ভাই, একদাম ৩০০ টাকা!'",
      DistrictId.MODERN_DHAKA, MissionGoalType.VEHICLE_RACE, 15f, -135f, 135f, -45f, 580L, 100
    ),
    MissionDef(
      "m27_tea_challenge", 27, "Tea Delivery Challenge", "চা ডেলিভারি চ্যালেঞ্জ",
      "Carry 3 hot cups of Cha from Old Dhaka stall to Commercial warehouse without crashing!",
      "রিকশা বা পায়ে হেঁটে পুরান ঢাকা থেকে বাণিজ্যিক গুদামে চা পৌঁছে দাও!",
      "Cha Mama: 'Don't spill a single drop!'",
      "চা মামা: 'এক ফোঁটা চা যেন না পড়ে!'",
      DistrictId.OLD_DHAKA, MissionGoalType.DELIVERY_RUN, -8f, 5f, 130f, -40f, 540L, 105
    ),
    MissionDef(
      "m28_angry_shopkeeper", 28, "The Angry Shopkeeper", "রাগী দোকানদার",
      "You bumped the soda crates! Knock down 5 cardboard boxes & escape the chase!",
      "দোকানের কার্টন পড়ে গেছে! ৫টি বক্স সরিয়ে দ্রুত পালিয়ে যাও!",
      "Shopkeeper: 'Hey! Who pays for my Mojo bottles?!'",
      "দোকানদার: 'এই ছেলে! আমার বোতলের দাম কে দেবে?!'",
      DistrictId.COMMERCIAL, MissionGoalType.PROP_SMASH, 135f, -50f, 145f, -35f, 490L, 95, 5
    ),
    MissionDef(
      "m29_riverboat", 29, "Riverboat Adventure", "নৌকা ভ্রমণ",
      "Board a wooden boat at Buriganga Ghat and sail across the river channel!",
      "বুড়িগঙ্গা ঘাট থেকে নৌকা চালিয়ে নদীর ওপারে ঘুরে এসো!",
      "Majhi: 'Row with the tide, feel the Buriganga breeze!'",
      "মাঝি: 'ভাটির টানে বৈঠা চালাও, বুড়িগঙ্গার হাওয়া খাও!'",
      DistrictId.RIVERSIDE, MissionGoalType.VEHICLE_RACE, -20f, 142f, 45f, 145f, 600L, 110
    ),
    MissionDef(
      "m30_midnight_food", 30, "Midnight Street Food Run", "মধ্যরাতের ফুচকা অভিযান",
      "Race across Dhaka to hit the glowing Old Dhaka midnight street food cart!",
      "পুরান ঢাকার রাতের ঝালমুড়ি ও ফুচকা ভ্যানে সবার আগে পৌঁছে যাও!",
      "Friend: 'Midnight cravings wait for no traffic jam!'",
      "বন্ধু: 'রাত বারোটায় ফুচকা আর হালিম না খেলে ঘুম আসবে না!'",
      DistrictId.MODERN_DHAKA, MissionGoalType.REACH_TARGET, 0f, -135f, 4f, 14f, 750L, 115
    ),
  )
}
