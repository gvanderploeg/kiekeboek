create or replace view actieve_personen as 
SELECT
p.id as persoonid
, case p.toon_roepnaam when 1 then p.roepnaam else p.initialen end as roepnaam
, p.tvg as tussenvoegsel
, p.achternaam as achternaam
, h.straat as straat
, h.postcode as postcode
, h.plaats as plaats
, p.geboorte_datum as geboortedatum
, case p.telefoon when "" then h.telefoon else p.telefoon end as telefoon
, case p.mobiel when "" then h.mobiel else p.mobiel end as mobiel
, case p.email when "" then h.email else p.email end as emailadres
, if (h.telefoon != p.telefoon and p.telefoon != "", h.telefoon, "") as huis_telefoon
, if (h.mobiel != p.mobiel and p.mobiel != "", h.mobiel, "") as huis_mobiel
, if (h.email != p.email and p.email != "", h.email, "") as huis_emailadres
, ltrim(concat_ws(" ", p.tvg, p.achternaam)) as achternaam_met_tussenvoegsel /* achternaam met evt. tussenvoegsel ervoor */
, case p.lw when 0000-00-00 then makedate(1970,1) else p.lw end as lastModified /* 1970 als er geen lw-waarde is */
FROM kiekeboek.persoon p
left outer join huis_persoon hp on (p.id = hp.pid)
left outer join huishouden h on (hp.hid = h.id)
where 1=1
and p.lid_status = 14 /* actief */
and p.toon_in_kiekenboek = 1 /* mag getoond worden */
and (hp.eind_datum is null or hp.eind_datum = 0000-00-00) /* actieve persoon-huishouden-relatie */
/* and date_add(p.geboorte_datum, interval 12 year) < now() */ /* alleen personen ouder dan 12 jaar */
order by h.label, h.id, roepnaam, geboortedatum