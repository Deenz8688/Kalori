<?php

include "db.php";

$email = $_POST['email'];
$new_password = $_POST['new_password'];

// 1. Semak sama ada emel user wujud atau tidak dlm database
$sql = "SELECT * FROM users WHERE email='$email'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {

    // 2. Encrypt password baharu supaya ngam dngan login.php tadi
    $hashed_password = password_hash($new_password, PASSWORD_DEFAULT);

    // 3. Kemas kini kata laluan baharu dlm database
    $sql_update = "UPDATE users SET password='$hashed_password' WHERE email='$email'";
    
    if ($conn->query($sql_update) === TRUE) {
        echo "success";
    } else {
        echo "Gagal mengemaskini kata laluan";
    }

} else {
    echo "User Not Found";
}

$conn->close();

?>