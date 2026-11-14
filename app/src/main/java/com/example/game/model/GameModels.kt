package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class AppLang { EN, BN }

enum class DistrictId(
  val titleEn: String,
  val titleBn: String,
  val centerX: Float,
  val centerZ: Float,
  val accentHex: Long,
) {
  OLD_DHAKA("Old Dhaka", "পুরান ঢাকা", 0f, 0f, 0xFFF42A41),
  MODERN_DHAKA("Modern Dhaka", "নতুন ঢাকা", 0f, -135f, 0xFF38BDF8),
  RESIDENTIAL("Residential Area", "আবাসিক এলাকা", -135f, 0f, 0xFF10B981),
  VILLAGE("Village Area", "গ্রাম বাংলা", -135f, -135f, 0xFF84CC16),
  COMMERCIAL("Commercial Hub", "বাণিজ্যিক এলাকা", 135f, -45f, 0xFFFFB703),
  RIVERSIDE("Buriganga Riverside", "বুড়িগঙ্গা ঘাট", 0f, 135f, 0xFF06B6D4),
}

enum class VehicleType(
  val titleEn: String,
  val titleBn: String,
  val maxSpeed: Float,
  val accel: Float,
  val turnSpeed: Float,
  val priceTaka: Long,
  val isBoat: Boolean = false,
  val primaryColor: Int,
  val secondaryColor: Int,
) {
  RICKSHAW("Rickshaw", "রিকশা", 14f, 18f, 2.8f, 450L, false, 0xFFE11D48.toInt(), 0xFFFACC15.toInt()),
  CNG("CNG Auto", "সিএনজি", 21f, 24f, 2.5f, 1500L, false, 0xFF15803D.toInt(), 0xFFFBBF24.toInt()),
  BICYCLE("Bicycle", "সাইকেল", 12f, 16f, 3.1f, 250L, false, 0xFF2563EB.toInt(), 0xFFE2E8F0.toInt()),
  MOTORCYCLE("Motorcycle", "মোটরসাইকেল", 28f, 32f, 2.9f, 2800L, false, 0xFFDC2626.toInt(), 0xFF1E293B.toInt()),
  SCOOTER("Scooter", "স্কুটার", 22f, 25f, 2.8f, 1800L, false, 0xFF0EA5E9.toInt(), 0xFFF8FAFC.toInt()),
  LOCAL_BUS("Local Bus", "লোকাল বাস", 20f, 14f, 1.4f, 5000L, false, 0xFFEA580C.toInt(), 0xFFFEF3C7.toInt()),
  MINI_TRUCK("Mini Truck", "মিনি ট্রাক", 19f, 15f, 1.7f, 4200L, false, 0xFF0284C7.toInt(), 0xFF334155.toInt()),
  PICKUP("Pickup Truck", "পিকআপ", 23f, 21f, 2.0f, 3800L, false, 0xFF0D9488.toInt(), 0xFF1E293B.toInt()),
  PRIVATE_CAR("Private Car", "প্রাইভেট কার", 30f, 28f, 2.3f, 6500L, false, 0xFFF8FAFC.toInt(), 0xFF334155.toInt()),
  DELIVERY_VAN("Delivery Van", "ডেলিভারি ভ্যান", 22f, 20f, 1.9f, 3200L, false, 0xFF7C3AED.toInt(), 0xFFFDE047.toInt()),
  BOAT("River Boat", "নৌকা", 15f, 14f, 2.1f, 1200L, true, 0xFF92400E.toInt(), 0xFFD97706.toInt()),
}

enum class WeatherType(val titleEn: String, val titleBn: String) {
  CLEAR("Sunny", "রোদেলা"),
  CLOUDY("Cloudy", "মেঘলা"),
  RAIN("Rain", "বৃষ্টি"),
  MONSOON("Monsoon Storm", "ঝুম বৃষ্টি"),
  FOG("Morning Fog", "কুয়াশা"),
}

enum class GraphicsQuality(val key: String, val titleEn: String, val titleBn: String, val drawDistance: Float, val maxParticles: Int) {
  LOW("low", "Low", "কম", 120f, 40),
  MEDIUM("medium", "Medium", "মাঝারি", 180f, 90),
  HIGH("high", "High", "উচ্চ", 260f, 180),
}

enum class ExpressionType(val titleEn: String, val titleBn: String, val emoji: String) {
  HAPPY("Happy", "খুশি", "😄"),
  ANGRY("Angry", "রাগী", "😠"),
  SHOCKED("Shocked", "অবাক", "😱"),
  CONFUSED("Confused", "বিভ্রান্ত", "😕"),
  LAUGHING("Laughing", "হাসি", "😂"),
  CRYING("Crying", "কান্না", "😭"),
  SURPRISED("Surprised", "বিস্মিত", "😲"),
}

enum class ShirtColor(val labelEn: String, val argb: Int) {
  EMERALD("Deshi Emerald", 0xFF059669.toInt()),
  CRIMSON("Lalbagh Red", 0xFFE11D48.toInt()),
  MARIGOLD("Genda Gold", 0xFFF59E0B.toInt()),
  PANJABI_WHITE("Eid White", 0xFFF1F5F9.toInt()),
  COBALT("Dhaka Blue", 0xFF2563EB.toInt()),
  PURPLE("Jamuna Purple", 0xFF7C3AED.toInt()),
}

enum class PantsColor(val labelEn: String, val argb: Int) {
  DENIM("Denim Blue", 0xFF1E3A8A.toInt()),
  LUNGI_CHECK("Lungi Check", 0xFF0F766E.toInt()),
  CHARCOAL("Charcoal", 0xFF1E293B.toInt()),
  KHAKI("Khaki", 0xFFA16207.toInt()),
}

enum class SkinTone(val labelEn: String, val argb: Int) {
  WARM("Warm", 0xFFC68642.toInt()),
  LIGHT("Light", 0xFFE0AC69.toInt()),
  DEEP("Deep", 0xFF8D5524.toInt()),
  TAN("Golden Tan", 0xFFB87333.toInt()),
}

enum class HairStyle(val labelEn: String, val labelBn: String) {
  CLASSIC("Classic Crop", "সাদামাটা"),
  SPIKY("Hero Spiky", "স্পাইকি"),
  CURLY("Dhaka Curly", "কোঁকড়ানো"),
  LONG("Artist Long", "লম্বা চুল"),
}

enum class HatStyle(val labelEn: String, val labelBn: String) {
  NONE("None", "কিছুই না"),
  GAMCHA("Gamcha Band", "গামছা"),
  CAP("Cricket Cap", "ক্যাপ"),
  TOPI("Panjabi Topi", "টুপি"),
  RICKSHAW_HAT("Straw Mathal", "মাথাল"),
}

enum class GlassesStyle(val labelEn: String, val labelBn: String) {
  NONE("None", "কিছুই না"),
  SHADES("Babu Shades", "সানগ্লাস"),
  ROUND("Poet Round", "গোল চশমা"),
  NERD("Exam Prep", "মোটা চশমা"),
}

enum class CameraAngleMode(val titleEn: String, val titleBn: String) {
  THIRD_PERSON("Third Person", "থার্ড পারসন"),
  CINEMATIC("Viral Orbit", "সিনেম্যাটিক"),
  LOW_ACTION("Low Hero Cam", "লো অ্যাকশন"),
  FIRST_PERSON("First Person", "ফার্স্ট পারসন"),
}

data class CharacterAppearance(
  val shirtColor: ShirtColor = ShirtColor.EMERALD,
  val pantsColor: PantsColor = PantsColor.DENIM,
  val skinTone: SkinTone = SkinTone.WARM,
  val hairStyle: HairStyle = HairStyle.CLASSIC,
  val hatStyle: HatStyle = HatStyle.NONE,
  val glassesStyle: GlassesStyle = GlassesStyle.NONE,
  val expression: ExpressionType = ExpressionType.HAPPY,
)

enum class NpcRole(val titleEn: String, val titleBn: String, val shirtColor: Int, val isAnimal: Boolean = false) {
  TEA_OWNER("Tea Stall Mama", "চা মামা", 0xFFD97706.toInt()),
  SHOPKEEPER("Shopkeeper", "দোকানদার", 0xFF0284C7.toInt()),
  RICKSHAW_DRIVER("Rickshaw Puller", "রিকশাচালক", 0xFF059669.toInt()),
  BUS_DRIVER("Bus Driver", "বাস ড্রাইভার", 0xFFEA580C.toInt()),
  STUDENT("Student", "ছাত্র", 0xFFF8FAFC.toInt()),
  TEACHER("Teacher", "শিক্ষক", 0xFF475569.toInt()),
  POLICE("Traffic Police", "পুলিশ", 0xFF1E3A8A.toInt()),
  VENDOR("Street Vendor", "হকার", 0xFF9333EA.toInt()),
  PEDESTRIAN("Pedestrian", "পথচারী", 0xFF64748B.toInt()),
  CHILD("Kid", "বাচ্চা", 0xFFF43F5E.toInt()),
  NEIGHBOR("Angry Uncle", "রাগী আঙ্কেল", 0xFF78350F.toInt()),
  DELIVERY_WORKER("Delivery Rider", "ডেলিভারি", 0xFFEC4899.toInt()),
  COW("Deshi Cow", "গরু", 0xFFE2E8F0.toInt(), true),
  GOAT("Naughty Goat", "ছাগল", 0xFF78716C.toInt(), true),
  CHICKEN("Market Chicken", "মুরগি", 0xFFFEF08A.toInt(), true),
  DOG("Street Dog", "কুকুর", 0xFFB45309.toInt(), true),
}

enum class InteractableKind(
  val titleEn: String,
  val titleBn: String,
  val actionEn: String,
  val actionBn: String,
) {
  TEA_STALL("Cha Stall", "চায়ের দোকান", "Drink Ginger Cha (৳15)", "আদা চা খাও (৳১৫)"),
  STREET_FOOD("Fuchka & Biryani", "ফুচকা ও বিরিয়ানি", "Eat Street Food (৳40)", "ফুচকা/বিরিয়ানি খাও (৳৪০)"),
  GROCERY_SHOP("Mudi Dokan", "মুদি দোকান", "Open Shop", "দোকানে ঢোকো"),
  VEHICLE_DEALER("Garage & Showroom", "গ্যারেজ ও শোরুম", "Buy / Spawn Vehicle", "গাড়ি কিনুন/নামান"),
  ELECTRIC_BOX("DESCO Fuse Box", "ইলেকট্রিক বক্স", "Flip Power Switch", "কারেন্ট সুইচ দাও"),
  CEILING_FAN("Ceiling Fan", "সিলিং ফ্যান", "Toggle Fan", "ফ্যান চালাও/বন্ধ করো"),
  TELEVISION("BTV Box TV", "টেলিভিশন", "Switch TV Channel", "টিভি চালু করো"),
  CHAIR("Plastic Chair", "প্লাস্টিক চেয়ার", "Sit & Chill", "চেয়ারে বসো"),
  BED("Charpai Bed", "বিছানা", "Rest & Advance Time", "ঘুমাও ও সেভ করো"),
  FRIDGE("Walton Fridge", "ফ্রিজ", "Open Fridge & Snack", "ফ্রিজ খোলো"),
  WATER_TAP("WASA Tap", "পানির কল", "Turn Water Tap", "পানির কল খোলো"),
  CRICKET_WICKET("Street Cricket", "গলি ক্রিকেট", "Smash Sixer!", "ছক্কা মারো!"),
  FOOTBALL("Football Pitch", "ফুটবল", "Kick Ball", "বলে লাথি মারো"),
  SLIPPER_PILE("Rubber Slipper", "রাবারের চপ্পল", "Pick Up Slipper", "চপ্পল তোলো"),
  SHOP_BELL("Shop Bell", "দোকানের ঘণ্টা", "Ring Shop Bell", "ঘণ্টা বাজাও"),
  FISHING_SPOT("Buriganga Ghat", "মাছ ধরার ঘাট", "Go Fishing (+৳)", "মাছ ধরো (+৳)"),
  HOME_DOOR("Your Dhaka Home", "তোমার বাসা", "Customize Home", "বাসা সাজাও"),
  LADDER("Rooftop Ladder", "ছাদের মই", "Climb to Rooftop", "ছাদে ওঠো"),
}

enum class PropKind(
  val labelEn: String,
  val labelBn: String,
  val breakable: Boolean,
  val colorArgb: Int,
  val halfSize: Float,
) {
  PLASTIC_CHAIR("Plastic Chair", "প্লাস্টিক চেয়ার", true, 0xFFDC2626.toInt(), 0.45f),
  WOODEN_TABLE("Wooden Table", "কাঠের টেবিল", true, 0xFF92400E.toInt(), 0.65f),
  CLAY_POT("Matir Kolshi", "মাটির কলসি", true, 0xFFB45309.toInt(), 0.35f),
  GLASS_BOTTLE("Soda Bottle", "কাঁচের বোতল", true, 0xFF10B981.toInt(), 0.22f),
  CARDBOARD_BOX("Parcel Box", "কার্টন বক্স", true, 0xFFD97706.toInt(), 0.45f),
  STREET_SIGN("Traffic Sign", "রাস্তার সাইন", true, 0xFFEAB308.toInt(), 0.40f),
  BUCKET("Water Bucket", "বালতি", false, 0xFF0284C7.toInt(), 0.32f),
  FOOTBALL_PROP("Football", "ফুটবল", false, 0xFFF8FAFC.toInt(), 0.28f),
  CRICKET_BALL("Tape Tennis Ball", "ক্রিকেট বল", false, 0xFFEF4444.toInt(), 0.16f),
  SLIPPER("Flying Slipper", "উড়ন্ত চপ্পল", false, 0xFF3B82F6.toInt(), 0.20f),
}

enum class ShopItemCategory { FOOD, ITEM, FURNITURE, UPGRADE }

data class ShopItem(
  val id: String,
  val nameEn: String,
  val nameBn: String,
  val priceTaka: Long,
  val category: ShopItemCategory,
  val sellValueTaka: Long = priceTaka / 2,
)

object ShopCatalog {
  val items = listOf(
    ShopItem("hot_cha", "Ginger Cha", "আদা চা", 15L, ShopItemCategory.FOOD),
    ShopItem("shingara", "Hot Shingara", "গরম সিঙ্গারা", 20L, ShopItemCategory.FOOD),
    ShopItem("fuchka", "Dhaka Fuchka", "ঝাল ফুচকা", 40L, ShopItemCategory.FOOD),
    ShopItem("kacchi", "Old Dhaka Kacchi", "পুরান ঢাকার কাচ্চি", 250L, ShopItemCategory.FOOD),
    ShopItem("rubber_chappal", "Hero Slipper", "চপ্পল অস্ত্র", 80L, ShopItemCategory.ITEM),
    ShopItem("cricket_bat", "Tape-Tennis Bat", "ক্রিকেট ব্যাট", 180L, ShopItemCategory.ITEM),
    ShopItem("ips_battery", "IPS Inverter", "আইপিএস ব্যাটারি", 900L, ShopItemCategory.ITEM),
    ShopItem("hilsha_fish", "Padma Ilish", "পদ্মার ইলিশ", 600L, ShopItemCategory.ITEM, 450L),
    ShopItem("ceiling_fan", "BRB Ceiling Fan", "সিলিং ফ্যান", 350L, ShopItemCategory.FURNITURE),
    ShopItem("charpai_bed", "Deshi Khat", "দেশি খাট", 500L, ShopItemCategory.FURNITURE),
    ShopItem("crt_tv", "Retro Color TV", "রঙিন টিভি", 700L, ShopItemCategory.FURNITURE),
    ShopItem("fridge", "Walton Fridge", "ফ্রিজ", 1100L, ShopItemCategory.FURNITURE),
    ShopItem("tea_table", "Adda Tea Table", "আড্ডার টেবিল", 300L, ShopItemCategory.FURNITURE),
    ShopItem("home_rooftop_garden", "Rooftop Garden", "ছাদ বাগান", 1500L, ShopItemCategory.UPGRADE),
    ShopItem("home_ips_solar", "Solar IPS Grid", "সোলার আইপিএস", 2200L, ShopItemCategory.UPGRADE),
  )
}
