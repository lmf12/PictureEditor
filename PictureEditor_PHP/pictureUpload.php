<?php

	require_once "index.php";

	$target_path  = "./upload/";//接收文件目录

	$file_name = $_FILES['uploadedfile']['name'];

	$edit_type = explode("@",$file_name)[0];
	$file_name = explode("@",$file_name)[1];

	$now_time = time();

	$target_path = $target_path.$now_time."_".basename( $file_name );

	if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {

		switch ($edit_type) {
			case '1':
				grayscale('./upload/'.$now_time."_".$file_name, './edit/'.$now_time."_".$file_name);
				break;
			case '2':
				blur('./upload/'.$now_time."_".$file_name, './edit/'.$now_time."_".$file_name);
				break;
			case '3':
				gammaCorrection('./upload/'.$now_time."_".$file_name, './edit/'.$now_time."_".$file_name);
				break;
			case '4':
				colorize('./upload/'.$now_time."_".$file_name, './edit/'.$now_time."_".$file_name);
				break;
			case '5':
				imageWatermarking('./upload/'.$now_time."_".$file_name, './edit/'.$now_time."_".$file_name);
				break;
			default:
				break;
		}
		$url = "/edit/".$now_time."_".$file_name;
	   	echo $url;
	}  else {

	   	echo "There was an error uploading the file, please try again!".$_FILES['uploadedfile']['error'];
	}


