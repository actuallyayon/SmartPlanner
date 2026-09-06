package com.smartplanner.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplanner.view.theme.PrimaryBlue
import com.smartplanner.view.theme.SuccessGreen

@Composable
fun ExploreScreen(
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        
        // --- HERO SECTION ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            // Pill badge
            Surface(
                color = PrimaryBlue.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.border(1.dp, PrimaryBlue.copy(alpha = 0.3f), CircleShape)
            ) {
                Text(
                    text = "✨ The Future of Habit Tracking",
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Headline
            Text(
                text = buildAnnotatedString {
                    append("Build routines that\n")
                    withStyle(style = SpanStyle(color = PrimaryBlue)) {
                        append("actually adapt.")
                    }
                },
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Subhead
            Text(
                text = "Adaptive habit stacks that automatically adjust to your schedule, tiredness, and real progress.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // CTAs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNavigateToDashboard,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Go to Dashboard", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { /* Scroll down or action */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) {
                    Text("See How it Works", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- TODAY'S CHECK-IN PREVIEW ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LIVE DEMO PREVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Habit checkin line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.1f))
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Morning Jog · 15 minutes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "\"Felt energetic today!\"",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // AI Chat bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp),
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp))
                    ) {
                        Text(
                            text = "Awesome jog! Rain is forecast tomorrow morning — I've swapped your routine to an indoor 10-minute session so you don't lose momentum.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // --- BUILT TO ADAPT SECTION ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Built to adapt to real life",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Traditional trackers fail when routines break. Smart Planner dynamically adjusts your stacks around your energy and schedule.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // 2x2 Feature Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Habit Stacking",
                        desc = "Anchor new habits to established daily triggers.",
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    FeatureCard(
                        title = "Weekly Replanning",
                        desc = "AI recalibrates your stack when obstacles arise.",
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Contextual Coaching",
                        desc = "Actionable tips matched to your daily energy.",
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    FeatureCard(
                        title = "Pattern Insights",
                        desc = "Spot consistency trends without feeling guilty.",
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }

        // --- HOW IT WORKS ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "How Smart Planner Works",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            val steps = listOf(
                "1. Set Goals" to "Define targets, daily constraints, and obstacles.",
                "2. Get Adaptive Stack" to "AI builds a personalized stack with backup mini-versions.",
                "3. Check-in & Evolve" to "Log quick updates and let AI optimize your routine weekly."
            )

            steps.forEach { (title, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // --- GOAL CATEGORIES GRID ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Popular Goal Categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            val cats = listOf(
                "Health & Fitness" to "12.5k planners",
                "Focus & Deep Work" to "8.2k planners",
                "Sleep Hygiene" to "5.4k planners",
                "Mindfulness" to "6.1k planners",
                "Learning & Skills" to "9.8k planners",
                "Career Growth" to "4.3k planners"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in cats.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(title = cats[i].first, count = cats[i].second, modifier = Modifier.weight(1f).fillMaxHeight())
                        CategoryCard(title = cats[i+1].first, count = cats[i+1].second, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        // --- STATS BAND ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatBandItem(num = "42,000+", label = "Active Plans")
                StatBandItem(num = "1.2M", label = "Checks Logged")
                StatBandItem(num = "84.2%", label = "Avg. Consistency")
            }
        }

        // --- TESTIMONIALS ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Loved by real people",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            val quotes = listOf(
                "\"The mini version saved my Spanish streak on busy days. 3 months unbroken!\"" to "— Sarah K.",
                "\"Adapts when my Wednesdays get swamped without guilt.\"" to "— Alex D.",
                "\"Like a habit coach that replans around my actual schedule.\"" to "— Marcus J."
            )

            quotes.forEach { (quote, author) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = quote,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = author,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // --- FAQ ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Frequently Asked Questions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            val faqList = listOf(
                "What is Habit Stacking?" to "Anchoring a new habit immediately after an existing daily routine.",
                "How does the AI adapt?" to "It learns from your check-ins and adjusts difficulty or triggers when you get stuck.",
                "Is data saved offline?" to "Yes, all your habits and check-ins are stored locally on your device."
            )

            faqList.forEach { (q, a) ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = q, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text(text = if (expanded) "▲" else "▼", fontSize = 12.sp, color = PrimaryBlue)
                        }
                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = a, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // --- PRICING ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Simple, transparent pricing",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Free card
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Free", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "$0/mo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "• 1 active stack\n• Basic check-in\n• Default triggers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Current Plan", fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }

                // Pro card (Popular)
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = PrimaryBlue,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "POPULAR",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Pro", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "$9/mo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "• Unlimited stacks\n• AI weekly coach\n• Advanced analytics", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Upgrade Pro", fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        // --- FOOTER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Divider()
            Text(
                text = "Smart Planner © 2026. Built with MVC Architecture.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "About", fontSize = 12.sp, color = PrimaryBlue)
                Text(text = "Privacy Policy", fontSize = 12.sp, color = PrimaryBlue)
                Text(text = "Terms of Service", fontSize = 12.sp, color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun FeatureCard(title: String, desc: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
            Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 16.sp)
        }
    }
}

@Composable
fun CategoryCard(title: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = count, fontSize = 12.sp, color = PrimaryBlue)
        }
    }
}

@Composable
fun StatBandItem(num: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}
