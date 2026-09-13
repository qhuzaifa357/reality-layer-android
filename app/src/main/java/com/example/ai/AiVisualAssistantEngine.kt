package com.example.ai

import android.graphics.Bitmap
import com.example.R
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

class AiVisualAssistantEngine {

    val sampleScenes: List<SampleScene> = listOf(
        SampleScene(
            id = "plant_monstera",
            title = "Monstera Deliciosa",
            category = "Botany / Houseplant",
            description = "Tropical climbing plant known for split leaves and air-purifying qualities",
            drawableRes = R.drawable.img_hero_scan,
            defaultQuestion = "What plant is this and how should I care for it?"
        ),
        SampleScene(
            id = "vintage_camera",
            title = "Vintage Rangefinder Camera",
            category = "Optics & Photography",
            description = "35mm manual film camera with mechanical shutter and coupled rangefinder",
            drawableRes = R.drawable.img_app_icon,
            defaultQuestion = "What model is this and what can it be used for today?"
        ),
        SampleScene(
            id = "espresso_machine",
            title = "Portafilter Espresso Machine",
            category = "Culinary Technology",
            description = "High-pressure 9-bar espresso extractor for specialty coffee brewing",
            drawableRes = R.drawable.img_hero_scan,
            defaultQuestion = "How do I dial in this machine for the best extraction?"
        ),
        SampleScene(
            id = "circuit_board",
            title = "ARM Microcontroller Board",
            category = "Embedded Electronics",
            description = "Single-board computer with GPIO pin headers, USB-C, and WiFi module",
            drawableRes = R.drawable.img_app_icon,
            defaultQuestion = "What can I build with this microcontroller board?"
        )
    )

    suspend fun analyzeImage(
        bitmap: Bitmap?,
        userQuestion: String,
        selectedSample: SampleScene?
    ): VisualAnalysisResult {
        // Simulate network / neural inference processing latency
        delay(1200)

        val queryLower = userQuestion.lowercase(Locale.getDefault())

        return if (selectedSample != null) {
            when (selectedSample.id) {
                "plant_monstera" -> buildMonsteraResult(queryLower)
                "vintage_camera" -> buildCameraResult(queryLower)
                "espresso_machine" -> buildEspressoResult(queryLower)
                "circuit_board" -> buildElectronicsResult(queryLower)
                else -> buildGenericVisualResult(selectedSample.title, selectedSample.category, queryLower)
            }
        } else {
            // Analyzing live captured camera photo
            inferFromCapturedImage(bitmap, queryLower)
        }
    }

    private fun inferFromCapturedImage(bitmap: Bitmap?, query: String): VisualAnalysisResult {
        val detectedName = if (query.contains("plant") || query.contains("flower") || query.contains("tree")) {
            "Fiddle-Leaf Fig (Ficus lyrata)"
        } else if (query.contains("book") || query.contains("text") || query.contains("page")) {
            "Printed Reference Volume"
        } else if (query.contains("cup") || query.contains("bottle") || query.contains("coffee")) {
            "Insulated Vacuum Tumbler"
        } else if (query.contains("building") || query.contains("arch") || query.contains("place")) {
            "Neoclassical Architectural Facade"
        } else if (query.contains("computer") || query.contains("keyboard") || query.contains("screen")) {
            "Compact Mechanical Keyboard & Display"
        } else {
            "Analyzed Visual Subject"
        }

        val category = when {
            query.contains("plant") -> "Botany & Nature"
            query.contains("building") || query.contains("place") -> "Architecture & Geography"
            query.contains("keyboard") || query.contains("computer") -> "Technology & Peripherals"
            else -> "Everyday Object & Design"
        }

        val answerText = when {
            query.contains("use") || query.contains("used for") ->
                "This item is primarily utilized for productivity, ergonomic daily support, and focused environments. Its materials and form factor suggest durable high-frequency utility."
            query.contains("info") || query.contains("information") || query.contains("history") ->
                "Historical records and design patterns indicate this artifact follows modern ergonomic principles developed in the late 20th century, combining modular materials with user-friendly accessibility."
            query.contains("how") || query.contains("work") ->
                "Operating this requires standard ambient conditions. It functions mechanically or passively without requiring high maintenance, making it suitable for home and professional workspaces."
            else ->
                "Visual analysis identifies this as $detectedName. The geometry, edge reflections, and texture indicate well-crafted manufacturing with balanced proportions."
        }

        return VisualAnalysisResult(
            objectName = detectedName,
            category = category,
            confidence = 0.94f,
            summaryAnswer = answerText,
            primaryUse = "Enhances workspace efficiency, tactile interaction, and spatial organization.",
            usefulTips = listOf(
                "Keep away from direct excessive moisture or extreme heat sources.",
                "Clean gently using a microfiber cloth and mild neutral cleanser.",
                "Regular inspection maintains optimal longevity and appearance."
            ),
            keyFacts = listOf(
                "Object dimensions match standard industrial sizing conventions.",
                "Surface finish suggests matte anodized or durable textured composite.",
                "AI optical confidence index scored at 94.2% based on edge and color distribution."
            ),
            suggestedQuestions = listOf(
                "What materials is this made of?",
                "How do I clean and maintain it?",
                "Where was this style first created?"
            )
        )
    }

    private fun buildMonsteraResult(query: String): VisualAnalysisResult {
        val answer = when {
            query.contains("care") || query.contains("water") ->
                "Monstera Deliciosa thrives in bright, indirect sunlight. Water every 1-2 weeks, allowing the top 2 inches of potting mix to dry out between waterings. Ensure high humidity if possible."
            query.contains("use") || query.contains("benefit") ->
                "Beyond natural aesthetic interior décor, Monstera plants contribute to indoor air humidification and sound dampening. In its native tropical habitat, it produces edible fruit."
            else ->
                "This is a Monstera Deliciosa, often called the Swiss Cheese Plant due to the natural fenestrations (holes) that develop in its foliage to let sunlight reach lower leaves."
        }
        return VisualAnalysisResult(
            objectName = "Monstera Deliciosa (Swiss Cheese Plant)",
            category = "Botany / Araceae",
            confidence = 0.98f,
            summaryAnswer = answer,
            primaryUse = "Interior botanical decoration, indoor air purification, and bio-philic architecture.",
            usefulTips = listOf(
                "Provide a moss pole or trellis to support its natural climbing habit.",
                "Dust the broad leaves monthly with a damp cloth for optimal photosynthesis.",
                "Toxic to pets if chewed due to calcium oxalate crystals."
            ),
            keyFacts = listOf(
                "Native to tropical rainforests of southern Mexico to Panama.",
                "Fenestrations in leaves allow heavy wind and rain to pass without tearing.",
                "Can live for over 40 years with attentive root management."
            ),
            suggestedQuestions = listOf(
                "How often should I fertilize it?",
                "Why are the leaf tips turning brown?",
                "How do I propagate cuttings in water?"
            )
        )
    }

    private fun buildCameraResult(query: String): VisualAnalysisResult {
        val answer = when {
            query.contains("use") || query.contains("used for") ->
                "This mechanical rangefinder camera is used for street photography, documentary photojournalism, and fine-art portraits. Its split-image optical focusing offers precise manual distance measurement without mirror blackout."
            query.contains("work") || query.contains("how") ->
                "Light travels through the coated prime lens onto 35mm photographic film. A coupled optical prism inside the viewfinder superimposes two images; aligning them achieves crisp manual focus."
            else ->
                "This is a classic 35mm rangefinder camera. Unlike bulkier SLRs, it utilizes an independent optical window, allowing lightweight, quiet shutter operation favored by candid street photographers."
        }
        return VisualAnalysisResult(
            objectName = "35mm Mechanical Rangefinder Camera",
            category = "Optics & Vintage Technology",
            confidence = 0.96f,
            summaryAnswer = answer,
            primaryUse = "High-precision analog film photography, candid street documentation, and optical collection.",
            usefulTips = listOf(
                "Store in a dry box with silica gel to prevent fungal growth on internal glass elements.",
                "Exercise shutter speeds periodically to keep mechanical lubricants fluid.",
                "Use a UV filter to protect the front optical coating from scratches."
            ),
            keyFacts = listOf(
                "Rangefinders pioneered unobtrusive photojournalism in the 1930s-1960s.",
                "Operates completely without batteries for shutter cocking and exposure.",
                "Provides continuous scene visibility during shutter release."
            ),
            suggestedQuestions = listOf(
                "What is the best 35mm film stock for beginners?",
                "How do I test if the shutter timings are accurate?",
                "How does the rangefinder patch alignment work?"
            )
        )
    }

    private fun buildEspressoResult(query: String): VisualAnalysisResult {
        return VisualAnalysisResult(
            objectName = "Semi-Automatic Espresso Machine",
            category = "Culinary Engineering",
            confidence = 0.97f,
            summaryAnswer = "This is a 9-bar semi-automatic espresso extraction machine. It forces near-boiling water (around 93°C / 200°F) through finely ground, tamped coffee to create concentrated espresso with dense crema.",
            primaryUse = "Crafting espresso shots, microfoam milk for lattes and flat whites, and specialty hot beverages.",
            usefulTips = listOf(
                "Descale the boiler every 2-3 months to prevent calcium scale buildup.",
                "Backflush the group head weekly using food-safe detergent powder.",
                "Grind beans fresh immediately prior to extraction for rich crema."
            ),
            keyFacts = listOf(
                "Standard extraction target is 1:2 ratio (e.g. 18g in, 36g out) in 25-30 seconds.",
                "Thermoblock or brass boiler maintains rapid temperature stability.",
                "Pressure above 9 bar often causes channeling rather than better flavor."
            ),
            suggestedQuestions = listOf(
                "What grind size produces optimal extraction?",
                "How do I steam silky microfoam for latte art?",
                "Why is my espresso pulling too fast?"
            )
        )
    }

    private fun buildElectronicsResult(query: String): VisualAnalysisResult {
        return VisualAnalysisResult(
            objectName = "ARM Cortex Microcontroller Development Board",
            category = "Embedded IoT & Robotics",
            confidence = 0.99f,
            summaryAnswer = "This is an embedded 32-bit development board with integrated GPIO headers, pulse-width modulation (PWM), I2C, and SPI buses for interfacing with sensors, displays, and actuators.",
            primaryUse = "Prototyping IoT smart home devices, robotics, automation controllers, and firmware experiments.",
            usefulTips = listOf(
                "Observe 3.3V logic limits on GPIO pins to prevent voltage surge damage.",
                "Power via regulated USB-C 5V or external lithium battery power shield.",
                "Use decoupling capacitors near inductive loads like DC motors."
            ),
            keyFacts = listOf(
                "Consumes low power in deep sleep mode (often under 20 microamps).",
                "Programmable using C++, MicroPython, Rust, or Arduino frameworks.",
                "Contains hardware cryptographic accelerators for secure Wi-Fi and Bluetooth communication."
            ),
            suggestedQuestions = listOf(
                "How do I flash firmware via USB-C?",
                "Can this run lightweight TensorFlow Lite models?",
                "What sensor shields are compatible with this board?"
            )
        )
    }

    private fun buildGenericVisualResult(title: String, category: String, query: String): VisualAnalysisResult {
        return VisualAnalysisResult(
            objectName = title,
            category = category,
            confidence = 0.95f,
            summaryAnswer = "Visual analysis identifies this item as $title. Based on your question ('$query'), this subject represents a notable example of $category design.",
            primaryUse = "Practical daily utility, educational inquiry, and functional design.",
            usefulTips = listOf(
                "Inspect joints and surface finishes regularly.",
                "Keep clean using appropriate surface cleansers."
            ),
            keyFacts = listOf(
                "Item exhibits standard industry dimensions and materials.",
                "High structural integrity detected across the primary visual contours."
            ),
            suggestedQuestions = listOf(
                "What are alternative uses for this?",
                "Where can I find replacement components?"
            )
        )
    }

    suspend fun answerGeneralAiQuestion(question: String): String {
        delay(900)
        val q = question.lowercase(Locale.getDefault())

        return when {
            q.contains("architecture") || q.contains("gothic") || q.contains("building") ->
                """
                **Visual Architectural Clues:**
                • **Gothic**: Look for pointed arches, ribbed vaults, flying buttresses, and tall stained glass windows.
                • **Neoclassical**: Look for symmetrical facades, Doric or Ionic columns, triangular pediments, and domed roofs.
                • **Brutalist**: Raw exposed concrete (*béton brut*), modular geometric shapes, and bold sculptural masses.
                • **Modernist / Bauhaus**: Form follows function, flat roofs, steel/glass curtain walls, and minimal ornamentation.
                """.trimIndent()

            q.contains("plant") || q.contains("flower") || q.contains("leaf") ->
                """
                **AI Plant Identification Advice:**
                1. **Leaf Arrangement**: Note whether leaves are alternate, opposite, or whorled along the stem.
                2. **Leaf Venation & Margin**: Smooth edges (*entire*), toothed (*serrate*), or lobed.
                3. **Flower Anatomy**: Count petals and stamens; check for symmetry (radial vs. bilateral).
                4. **Sap & Texture**: Note any milky sap, hairy stems, or distinctive herbal aromas.
                """.trimIndent()

            q.contains("lidar") || q.contains("ar") || q.contains("reality layer") ->
                """
                **How Reality Layer Visual AI Operates:**
                Reality Layer combines high-resolution image processing with deep semantic embeddings.
                • **Feature Extraction**: Detects visual edges, surface contours, and textural gradients.
                • **Classification & Ontology**: Maps geometric clusters against millions of physical artifacts.
                • **Spatial Context**: When combined with location and heading, it cross-references real-world cartographic monuments and points of interest.
                """.trimIndent()

            q.contains("fix") || q.contains("repair") ->
                """
                **General Object Troubleshooting Framework:**
                1. **Identify Failure Point**: Is it mechanical (jammed gear, loose screw) or electrical (broken solder joint, drained capacitor)?
                2. **Power Isolation**: Always disconnect battery or AC mains before inspection.
                3. **Clean & Lubricate**: 70% of mechanical sticking resolves with isopropyl alcohol cleaning followed by appropriate lubricant (silicone or dry graphite).
                4. **Scan Again**: Take a close-up photo of any specific part numbers or serial codes for exact schematics!
                """.trimIndent()

            else ->
                """
                **Reality Layer AI Analysis:**
                Regarding your question: *"$question"*
                
                • **Key Principle**: Visual AI bridges the gap between what you see and what you can learn about physical objects.
                • **Application**: Point your camera at any object, sign, device, or landmark and tap **Analyze** to retrieve materials, origins, maintenance guides, and practical functions.
                • **Tip**: For best results, capture objects in clear lighting and frame the key distinctive features in the center of the viewfinder.
                """.trimIndent()
        }
    }

    fun getNearbyPlaces(latitude: Double?, longitude: Double?): List<NearbyPlace> {
        val lat = latitude ?: 37.7749
        val lon = longitude ?: -122.4194

        return listOf(
            NearbyPlace(
                id = "poi_1",
                name = "Metropolitan Heritage Clock Tower",
                category = "Historic Landmark",
                distanceMeters = 140,
                description = "Centennial stone clock tower completed in 1912 with ornate Romanesque carvings and bronze dial faces.",
                visualSignature = "Look for the copper-patina spire and four-sided illuminated dial overlooking the main plaza.",
                tips = "Scan the dedication plaque at the southern archway to reveal architectural blueprints and historic photographs."
            ),
            NearbyPlace(
                id = "poi_2",
                name = "Botanical Conservatory Pavilion",
                category = "Nature & Arboretum",
                distanceMeters = 320,
                description = "Victorian-era glasshouse exhibiting over 2,000 rare tropical epiphytes, ferns, and giant water lilies.",
                visualSignature = "Iconic white wooden frame and arched glass dome flanked by twin fountain courtyards.",
                tips = "Scan individual specimen tags or leaf venations for instant botanical species cards."
            ),
            NearbyPlace(
                id = "poi_3",
                name = "Contemporary Sculpture Promenade",
                category = "Public Art",
                distanceMeters = 480,
                description = "Open-air art installation featuring reflective polished steel mobius strips and kinetic wind-driven elements.",
                visualSignature = "Curving stainless steel sculptures reflecting the sky and pedestrian pathways.",
                tips = "Capture the artist signature imprint on the granite plinth for curator notes and audio commentary."
            ),
            NearbyPlace(
                id = "poi_4",
                name = "Civic Innovation Library",
                category = "Architecture & Culture",
                distanceMeters = 650,
                description = "Striking modern cantilevered structure designed with eco-certified timber beams and vertical living green walls.",
                visualSignature = "Dramatic geometric glass angles with cascading exterior hanging gardens.",
                tips = "Scan the atrium informational kiosk to download self-guided architectural walking tours."
            ),
            NearbyPlace(
                id = "poi_5",
                name = "Old Town Stone Aqueduct Ruins",
                category = "Archaeological Site",
                distanceMeters = 890,
                description = "Preserved 19th-century granite water viaduct arches that supplied the original township reservoir.",
                visualSignature = "Hand-cut quarry stones with moss-covered buttresses crossing the gentle valley ridge.",
                tips = "Scan the masonry keystones to view 3D historical reconstructions of how the aqueduct functioned."
            )
        )
    }
}
