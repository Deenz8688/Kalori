<?php

include "db.php";

$user_id = $_POST['user_id'];
$meal_type = $_POST['meal_type'];
$food_name = $_POST['food_name'];
$calories = $_POST['calories'];
$food_date = $_POST['food_date'];

// 🔥 TANGKAP 3 PARAMETER BARU DARI KOTLIN ANDROID STUDIO
$weight = isset($_POST['weight']) ? $_POST['weight'] : null;
$bmr = isset($_POST['bmr']) ? $_POST['bmr'] : null;
$tdee = isset($_POST['tdee']) ? $_POST['tdee'] : null;

$check = "SELECT * FROM foods
WHERE user_id='$user_id'
AND meal_type='$meal_type'
AND food_date='$food_date'";

$result = $conn->query($check);

if ($result->num_rows > 0) {

    $row = $result->fetch_assoc();

    $oldFood = $row['food_name'];
    $oldCalories = $row['calories'];

    $newFood = $oldFood . ", " . $food_name;
    $newCalories = $oldCalories + $calories;

    // 🔥 JIKA UPDATE: Gabung makanan + kemas kini berat, bmr, tdee yang paling terkini
    $sql = "UPDATE foods SET
    food_name='$newFood',
    calories='$newCalories',
    weight='$weight',
    bmr='$bmr',
    tdee='$tdee'
    WHERE user_id='$user_id'
    AND meal_type='$meal_type'
    AND food_date='$food_date'";

} else {

    // 🔥 JIKA INSERT: Masukkan data baru sekali dengan 3 kolum profil semasa
    $sql = "INSERT INTO foods
    (user_id, meal_type, food_name, calories, food_date, weight, bmr, tdee)
    VALUES
    ('$user_id', '$meal_type', '$food_name', '$calories', '$food_date', '$weight', '$bmr', '$tdee')";
}

if ($conn->query($sql) === TRUE) {

    echo "Food Saved";

} else {

    echo "Save Failed";
}

$conn->close();

?>