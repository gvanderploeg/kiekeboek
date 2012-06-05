<?php
/**
 * Script voor export van persoonsgegevens in json-formaat
 *
 * v1, GP, 2012-02-16
 */

require_once "intranet/phpSecurePages/secure.php";
include_once('intranet/common_settings.php');
require_once('intranet/cnx_mysql.php'); 

/*
 * @param since aantal seconden sinds 1970 (unix timestamp)
 * Als niet gegeven dan wordt alle data teruggegeven.
 */
if (isset($_GET['since'])) {
	$initieelRequest = false;
	$sindsParam = $_GET['since'];
	$sinds = (ctype_digit($sindsParam) && $sindsParam > 0) ? $sindsParam : 0;
} else {
	$initieelRequest = true;
}

/*
 * Bij een initieel request worden alleen actieve leden gegeven.
 * Bij opvolgende requests (met een tijd-parameter) worden alle mutaties sindsdien, en dus ook niet-actieve leden, gegeven.
 */
$alleenActieveLeden = $initieelRequest ? "and p.lid_status = 14" : "";
$alleenGewijzigdSinds = $initieelRequest ? "" : "and p.lw > from_unixtime($sinds) or h.lw > from_unixtime($sinds)";

$sql = "
SELECT
p.id as persoonid
, case p.toon_roepnaam when 1 then p.roepnaam else p.initialen end as roepnaam
, p.tvg as tussenvoegsel
, p.achternaam as achternaam
, h.straat as straat
, h.postcode as postcode
, h.plaats as plaats
, h.wijk as wijk
, kg.label as kleine_groep
, p.geboorte_datum as geboortedatum
, case p.telefoon when '' then h.telefoon else p.telefoon end as telefoon
, case p.mobiel when '' then h.mobiel else p.mobiel end as mobiel
, case p.email when '' then h.email else p.email end as emailadres
, if (h.telefoon != p.telefoon and p.telefoon != '', h.telefoon, '') as huis_telefoon
, if (h.mobiel != p.mobiel and p.mobiel != '', h.mobiel, '') as huis_mobiel
, if (h.email != p.email and p.email != '', h.email, '') as huis_emailadres
, ltrim(concat_ws(' ', p.tvg, p.achternaam)) as achternaam_met_tussenvoegsel /* achternaam met evt. tussenvoegsel ervoor */
, if (p.lid_status = 14, 'actief', 'niet-actief') as status
FROM persoon p
left outer join huis_persoon hp on (p.id = hp.pid)
left outer join huishouden h on (hp.hid = h.id)
left outer join adm_kleine_groep kg on (h.kleine_groep_id = kg.id)
where 1=1
and (hp.eind_datum is null or hp.eind_datum = 0000-00-00) /* actieve persoon-huishouden-relatie */
$alleenActieveLeden
$alleenGewijzigdSinds
order by h.label, h.id, roepnaam, geboortedatum
";
$query = mysql_query($sql);
$result = array();
while($row = mysql_fetch_assoc($query)) {
   $result[] = $row;
}
header("Content-Type: application/json");

$resultWithMetadata = array(
	"version" => 1,
	"since" => $initieelRequest ? 0 : $sinds,
	"data" => $result
);
echo json_encode($resultWithMetadata);

?>