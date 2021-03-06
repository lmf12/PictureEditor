<?php

require_once "vendor/autoload.php";

/*反向*/
function negative($soure, $target) {
	$imagine = new Imagine\Gd\Imagine();

	$image = $imagine->open($soure);

	$image->effects()
	    ->negative()
	    ->gamma(1.3);

	$image->save($target);
}

/*黑白*/
function grayscale($soure, $target) {
	$imagine = new Imagine\Gd\Imagine();

	$image = $imagine->open($soure);

	$image->effects()
	    ->grayscale();

	$image->save($target);
}


/*滤镜*/
function gammaCorrection($soure, $target) {
	$imagine = new Imagine\Gd\Imagine();

	$image = $imagine->open($soure);

	$image->effects()
	    ->gamma(0.7);

	$image->save($target);
}


/*色彩化*/
function colorize($soure, $target, $color) {
	$imagine = new Imagine\Gd\Imagine();

	$image = $imagine->open($soure);

	$pink = $image->palette()->color($color);

	$image->effects()
	    ->colorize($pink);

	$image->save($target);
}



/*模糊*/
function blur($soure, $target) {
	$imagine = new Imagine\Gd\Imagine();

	$image = $imagine->open($soure);

	$image->effects()
	    ->blur(3);

	$image->save($target);
}


/*加水印*/
function imageWatermarking($soure, $target) {
	$imagine = new Imagine\Gd\Imagine();

	$watermark = $imagine->open('./img/watermark.png');
	$image     = $imagine->open($soure);
	$size      = $image->getSize();
	$wSize     = $watermark->getSize();

	$bottomRight = new Imagine\Image\Point(($size->getWidth() - $wSize->getWidth())/2, $size->getHeight() - $wSize->getHeight());

	$image->paste($watermark, $bottomRight);
	$image->save($target);
}



/*以下为人脸识别功能*/
/*
功能一：眼镜
功能二：测年龄，性别
功能三：帽子
功能四：变脸
功能五：比较相似度
*/


// $attribute = "glass,pose,gender,age,race,smiling";

/*年龄识别*/
function ageJudgement($img_url) {

	$img_url = urlencode($img_url);
	$api_key = "567e6e7aeab4003e96d7088201356ce9";
	$api_secret = "gqBNR0lFhu1FZU7xtFdDWZgOxlkbKcb3";
	$attribute = "age,gender";
	$request_url = "http://apicn.faceplusplus.com/v2/detection/detect?api_key=".$api_key."&api_secret=".$api_secret."&url=".$img_url."&attribute=".$attribute;
	$re=file_get_contents($request_url);

	return $re;
}

/*戴眼镜*/
function glasses($img_url) {

	$img_url = urlencode($img_url);
	$api_key = "567e6e7aeab4003e96d7088201356ce9";
	$api_secret = "gqBNR0lFhu1FZU7xtFdDWZgOxlkbKcb3";
	$request_url = "http://apicn.faceplusplus.com/v2/detection/detect?api_key=".$api_key."&api_secret=".$api_secret."&url=".$img_url;
	$re=file_get_contents($request_url);

	return $re;
}

/*戴帽子*/
function hat($img_url) {

	$img_url = urlencode($img_url);
	$api_key = "567e6e7aeab4003e96d7088201356ce9";
	$api_secret = "gqBNR0lFhu1FZU7xtFdDWZgOxlkbKcb3";
	$request_url = "http://apicn.faceplusplus.com/v2/detection/detect?api_key=".$api_key."&api_secret=".$api_secret."&url=".$img_url;
	$re=file_get_contents($request_url);

	return $re;
}

/*变脸*/
function replaceFace($img_url) {

	$img_url = urlencode($img_url);
	$api_key = "567e6e7aeab4003e96d7088201356ce9";
	$api_secret = "gqBNR0lFhu1FZU7xtFdDWZgOxlkbKcb3";
	$request_url = "http://apicn.faceplusplus.com/v2/detection/detect?api_key=".$api_key."&api_secret=".$api_secret."&url=".$img_url;
	$re=file_get_contents($request_url);

	return $re;
}

/*相似度*/
function similar($img_url) {

	$img_url = urlencode($img_url);
	$api_key = "567e6e7aeab4003e96d7088201356ce9";
	$api_secret = "gqBNR0lFhu1FZU7xtFdDWZgOxlkbKcb3";
	$attribute = "age,gender";
	$request_url = "http://apicn.faceplusplus.com/v2/detection/detect?api_key=".$api_key."&api_secret=".$api_secret."&url=".$img_url."&attribute=".$attribute;
	$re=file_get_contents($request_url);

	$obj=json_decode($re); 

	$faces = $obj->face;

	if (count($faces) >= 2) {
		$face_id_1 = $faces[0]->face_id;
		$face_id_2 = $faces[1]->face_id;

		$url = "https://apicn.faceplusplus.com/v2/recognition/compare?api_secret=".$api_secret."&api_key=".$api_key."&face_id2=".$face_id_2."&face_id1=".$face_id_1;
		$result=$re."@@@".file_get_contents($url);

		return $result;
	}
	else {
		return $re;
	}
}













