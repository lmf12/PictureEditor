<?php 

	$hostdir=dirname(__FILE__);
	$upload = $hostdir."/upload";
	$filesnames = scandir($upload);


	$begin = '2016-02-08';
	$begin_time = strtotime($begin);
 
 	$now = date('Y-m-d');
 	$now_time = strtotime($now);

 	$days = round(($now_time - $begin_time)/3600/24);


	for ($i=0; $i<=$days; $i++) {

		$todayNum = 0;
		$t = date('Y-m-d',strtotime('-'.($days-$i).' day'));
		foreach ($filesnames as $name) {

			if ($name == "." && $name == "..") {
				continue;
			}

			$time = filemtime($upload."/".$name);

			if ($t == date('Y-m-d',$time)) {
		    	$todayNum++;
			}
		}

		echo "picture's number of ".$t." :".$todayNum;
		echo "<br>";
	}

	

?>