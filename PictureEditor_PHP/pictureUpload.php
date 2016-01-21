<?php

	require_once "index.php";

	$target_path  = "./upload/";//接收文件目录

	$file_name = $_FILES['uploadedfile']['name'];

	$edit_type = explode("@",$file_name)[0];
	$file_name = explode("@",$file_name)[1];

	$target_path = $target_path.basename( $file_name );

	if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {

		switch ($edit_type) {
			case '1':
				grayscale('./upload/'.$file_name, './edit/'.$file_name);
				break;
			case '2':
				blur('./upload/'.$file_name, './edit/'.$file_name);
				break;
			case '3':
				gammaCorrection('./upload/'.$file_name, './edit/'.$file_name);
				break;
			case '4':
				colorize('./upload/'.$file_name, './edit/'.$file_name);
				break;
			case '5':
				imageWatermarking('./upload/'.$file_name, './edit/'.$file_name);
				break;
			default:
				break;
		}
		$url = "/edit/".$file_name;
	   	echo $url;
	}  else {

	   	echo "There was an error uploading the file, please try again!".$_FILES['uploadedfile']['error'];
	}


