<?php

include "db.php";

$user_id = $_POST['user_id'];
$full_name = $_POST['full_name'];
$profile_image = $_POST['profile_image'];
$activity_level = $_POST['activity_level'];
$gender = $_POST['gender'];
$age = $_POST['age'];
$weight = $_POST['weight'];
$height = $_POST['height'];
$bmi = $_POST['bmi'];
$bmr = $_POST['bmr'];
$tdee = $_POST['tdee'];

$check = "SELECT * FROM profiles WHERE user_id='$user_id'";
$result = $conn->query($check);

if ($result->num_rows > 0) {

    $sql = "UPDATE profiles SET
        full_name='$full_name',
        profile_image='$profile_image',
        activity_level='$activity_level',
        gender='$gender',
        age='$age',
        weight='$weight',
        height='$height',
        bmi='$bmi',
        bmr='$bmr',
        tdee='$tdee'
        WHERE user_id='$user_id'";

} else {

    $sql = "INSERT INTO profiles
    (user_id, full_name, profile_image, activity_level,
    gender, age, weight, height, bmi, bmr, tdee)

    VALUES
    ('$user_id', '$full_name', '$profile_image',
    '$activity_level', '$gender', '$age',
    '$weight', '$height', '$bmi', '$bmr', '$tdee')";
}

if ($conn->query($sql) === TRUE) {
    echo "Profile Saved";
} else {
    echo "Save Failed";
}

$conn->close();

?>