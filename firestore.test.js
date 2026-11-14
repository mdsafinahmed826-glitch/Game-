const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_dhaka_01";
const BOB_UID = "bob_chittagong_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfile(uid) {
  const now = new Date();
  return {
    userId: uid,
    displayName: "Rafiq Vai",
    language: "en",
    graphicsQuality: "high",
    createdAt: now,
    updatedAt: now,
  };
}

function validSaveSlot(uid) {
  const now = new Date();
  return {
    userId: uid,
    slotName: "Auto Save",
    posX: 12.5,
    posY: 0.0,
    posZ: -24.0,
    yaw: 1.57,
    district: "OLD_DHAKA",
    moneyTaka: 1500,
    chaosLevel: 1,
    timeOfDay: 14.5,
    weather: "CLEAR",
    ownedVehicles: ["RICKSHAW", "BICYCLE"],
    completedMissions: ["m01_ips_war"],
    unlockedDistricts: ["OLD_DHAKA", "MODERN_DHAKA"],
    inventoryItems: ["cha_cup", "shingara"],
    homeFurniture: ["ceiling_fan", "charpai_bed"],
    homeLevel: 1,
    shirtColor: "TEAL",
    pantsColor: "DENIM",
    skinTone: "WARM",
    hairStyle: "SPIKY",
    hatStyle: "GAMCHA",
    glassesStyle: "SHADES",
    expression: "HAPPY",
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user cannot read or write user profiles or save slots", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("save_slots").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID))
  );
});

test("Authenticated owner can create, read, and update their profile and save slot", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  const slotRef = profileRef.collection("save_slots").doc("autosave");

  await assertSucceeds(profileRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(profileRef.get());
  await assertSucceeds(
    profileRef.update({ language: "bn", updatedAt: new Date() })
  );

  await assertSucceeds(slotRef.set(validSaveSlot(ALICE_UID)));
  await assertSucceeds(profileRef.collection("save_slots").get());
  await assertSucceeds(
    slotRef.update({ moneyTaka: 2500, updatedAt: new Date() })
  );
});

test("Cross-user access to profiles and save slots is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("save_slots")
      .doc("autosave")
      .set(validSaveSlot(ALICE_UID))
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("save_slots").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("save_slots")
      .doc("autosave")
      .update({ moneyTaka: 999999, updatedAt: new Date() })
  );
});

test("Shadow update with ghost field or invalid enum is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const slotRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("save_slots")
    .doc("autosave");

  const withGhost = { ...validSaveSlot(ALICE_UID), isAdmin: true };
  await assertFails(slotRef.set(withGhost));

  const badDistrict = { ...validSaveSlot(ALICE_UID), district: "MARS" };
  await assertFails(slotRef.set(badDistrict));

  const negativeMoney = { ...validSaveSlot(ALICE_UID), moneyTaka: -50 };
  await assertFails(slotRef.set(negativeMoney));
});
