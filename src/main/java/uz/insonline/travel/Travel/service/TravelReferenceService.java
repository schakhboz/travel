package uz.insonline.travel.Travel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.Travel.payload.request.reference.*;
import uz.insonline.travel.Travel.payload.response.reference.*;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.log.service.LogService;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TravelReferenceService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final LogService logService;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    public boolean isSite() {
        long tbId = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getTbId();
        return tbId == 1631 || tbId == (datasourceUrl.contains("test") ? 2036 : 2531);
    }

    private String getInsAbroadType(int lang) {
        return """
                select INS_ID as id, INS_NAME%d as name\s
                from INS_ABROAD_TYPE
                """.formatted(lang);
    }

    private String getInsAbroadGroups(int lang) {
        return """
                select g.INS_ID as id, g.INS_NAME%d as name\s
                              from INS_ABROAD_GROUP g\s
                             where g.INS_ACTIVE = 1\s
                               and g.INS_ID = (case
                                                  when :activity != 0 and :activity not in (select a.INS_ID from INS_ABROAD_ACTIVITY a where a.INS_ID != 0) then -1
                                                   when :activity != 0 then 0
                                                   else g.INS_ID end)
                """
                .formatted(lang);
    }

    private String getInsAbroadActivities(int lang) {
        return """
                select ins_id as id, ins_name%d as name from INS_ABROAD_ACTIVITY where INS_ID != 3
                """.formatted(lang);
    }

    private static String getInsAbroadMulti(int lang) {
        return """
                SELECT INS_ID   AS ID,
                       INS_DAYS AS DAYS,
                       INS_NAME || ': ' || INS_NAME%d AS NAME
                FROM INS_ABROAD_MULTI
                where INS_ACTIVE = 1 and 1 = coalesce(?, 1) order by INS_ID
                """.formatted(lang);
    }

    private static String getInsAbroadCountries(String language) {
        String fieldName;
        switch (language) {
            case "en" -> fieldName = "SP_ENAME";
            case "uz" -> fieldName = "SP_NAME3";
            case "uz-cyrl" -> fieldName = "SP_NAME2";
            default -> fieldName = "SP_NAME1";
        }
        return """
                select SP_FOND_ID     as              id,
                       trim(%s) as              name,
                       SP_KOD_ALPHA_2 as sp_code,
                       shengen,
                       (case when pr1 = 1 then 1 end) pr1,
                       (case when pr2 = 1 then 2 end) pr2,
                       (case when pr3 = 1 then 3 end) pr3,
                       (case when pr4 = 1 then 4 end) pr4,
                       (case when pr5 = 1 then 5 end) pr5
                from SP_COUNTRY
                where SP_ID not in (182)
                  and SP_ACTIVE = 1
                  AND SP_FOND_ID is not null
                order by name
                """.formatted(fieldName);
    }

    private final static String GET_INS_ABROAD_PROGRAMS = """
            select ins_id       as id,
                   PROGRAM_NAME as name
            from INS_ABROAD_PROGRAM
            where ACTIVE = 1
            order by 1
            """;

    private final static String GET_INS_ABROAD_PROGRAMS_BY_COUNTRIES = """
            select p.ins_id       as id,
                   p.program_name as name,
                   o.otv          as liability,
                   o.medex        as medicine,
                   o.accident,
                   o.covid,
                   o.evacuation,
                   o.transport,
                   o.compensation,
                   nvl(o.dentistry, 0) as dentistry,
                   nvl(o.repatriation, 0) as repatriation,
                   nvl(o.search_rescue, 0) as search_rescue,
                   nvl(o.third_party_visit, 0) as third_party_visit
            from INS_ABROAD_PROGRAM p
                     inner join INS_ABROAD_OTV o on o.program_id = p.ins_id
            where (p.INS_ID = 1 and
                   (select min(pr1)
                    from sp_country
                    where sp_fond_id in (select COLUMN_VALUE from table (split(:countries, ':')))) = 1
                )
               or (p.INS_ID = 2 and
                   (select min(pr2)
                    from sp_country
                    where sp_fond_id in (select COLUMN_VALUE from table (split(:countries, ':')))) = 1
                )
               or (p.INS_ID = 3 and
                   (select min(pr3)
                    from sp_country
                    where sp_fond_id in (select COLUMN_VALUE from table (split(:countries, ':')))) = 1
                )
               or (p.INS_ID = 4 and
                   (select min(pr4)
                    from sp_country
                    where sp_fond_id in (select COLUMN_VALUE from table (split(:countries, ':')))) = 1
                )
               or (p.INS_ID = 5 and
                   (select min(pr5)
                    from sp_country
                    where sp_fond_id in (select COLUMN_VALUE from table (split(:countries, ':')))) = 1
                )
               or (p.INS_ID = 6 and
                   (select min(pr6)
                    from sp_country
                    where sp_fond_id in (select COLUMN_VALUE from table (split(:countries, ':')))) = 1)
                and p.ACTIVE = 1
            order by case when p.INS_ID = 1 then 0 when p.INS_ID = 6 then 1 else 2 end, p.INS_ID
            """;

    private static final String GET_INS_ABROAD_PROGRAMS_BY_COUNTRIES_V2 = """
            select iap.ins_id                    as id,
                   iap.program_name              as name,
                   o.otv                         as liability,
                   o.medex                       as medicine,
                   o.accident,
                   o.covid,
                   o.evacuation,
                   o.transport,
                   o.compensation,
                   nvl(o.dentistry, 0)           as dentistry,
                   nvl(o.repatriation, 0)        as repatriation,
                   nvl(o.search_rescue, 0)       as search_rescue,
                   nvl(o.third_party_visit, 0)   as third_party_visit
            from ins_abroad_program iap
                     inner join ins_abroad_otv o on o.program_id = iap.ins_id
            cross join (
                select min(pr1) as pr1,
                       min(pr2) as pr2,
                       min(pr3) as pr3,
                       min(pr4) as pr4,
                       min(pr5) as pr5,
                       min(pr6) as pr6
                from sp_country
                where sp_fond_id in (:countryIds)
            ) cm
            where (
                    (iap.ins_id = 1 and cm.pr1 = 1 and :activity != 2)
                 or (iap.ins_id = 2 and cm.pr2 = 1)
                 or (iap.ins_id = 3 and cm.pr3 = 1)
                 or (iap.ins_id = 4 and cm.pr4 = 1)
                 or (iap.ins_id = 5 and cm.pr5 = 1)
                 or (iap.ins_id = 6 and cm.pr6 = 1)
            )
              and iap.active = 1
              and (
                    iap.ins_id != 6
                 or (
                        :activity = 2
                    and not exists (
                            select 1 from sp_country
                            where sp_fond_id in (:countryIds)
                              and sp_fond_id in (204, 55, 240, 254, 101)
                        )
                    )
              )
            order by case
                         when iap.ins_id = 1 then 0
                         when iap.ins_id = 6 then 1
                         else 2
                     end,
                     iap.ins_id
            """;

    public GetTravelTypesResponse getTravelTypes(String language) {

        List<TravelType> types = jdbcTemplate.query(getInsAbroadType(getLangCode(language)),
                (rs, rowNum) -> new TravelType(rs.getInt("id"), rs.getString("name")));

        return new GetTravelTypesResponse(types);
    }

    public GetTravelGroupsResponse getTravelGroups(Integer activityTypeId, String language) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("activity", activityTypeId); // Add the list of countries to the map
        List<TravelGroup> groups = namedParameterJdbcTemplate.query(getInsAbroadGroups(getLangCode(language)), params,
                (rs, rowNum) -> new TravelGroup(rs.getInt("id"), rs.getString("name")));

        return new GetTravelGroupsResponse(groups);
    }

    public GetTravelActivitiesResponse getTravelActivities(String language) {

        List<TravelActivity> activities = jdbcTemplate.query(getInsAbroadActivities(getLangCode(language)),
                (rs, rowNum) -> new TravelActivity(rs.getInt("id"), rs.getString("name")));

        return new GetTravelActivitiesResponse(activities);
    }

    // Method to fetch data from database and convert it to response object
    public GetTravelCountriesResponse getTravelCountries(String language) {
        Map<Integer, String> programMap = getProgramMap();
        List<TravelCountry> travelCountries = jdbcTemplate.query(getInsAbroadCountries(language),
                (resultSet, rowNum) -> {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int isInSchengen = resultSet.getInt("shengen");
                    String spCode = resultSet.getString("sp_code");

                    TravelCountry country = new TravelCountry();
                    country.setId(id);
                    country.setName(name);
                    country.setSp_code(spCode);
                    country.setIsInSchengen(isInSchengen);
                    country.setPrograms(new ArrayList<>());

                    // Adding programs to the country
                    for (int i = 1; i <= 4; i++) {
                        for (Map.Entry<Integer, String> entry : programMap.entrySet()) {
                            if (resultSet.getInt("pr" + i) == entry.getKey()) {
                                country.getPrograms().add(new TravelProgram(entry.getKey(), entry.getValue()));
                                break;
                            }
                        }
                    }

                    return country;
                });

        return new GetTravelCountriesResponse(travelCountries);
    }

    // Method to fetch programs for a given country ID
    private Map<Integer, String> getProgramMap() {

        Map<Integer, String> programMap = new HashMap<>();
        jdbcTemplate.query(GET_INS_ABROAD_PROGRAMS, resultSet -> {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            programMap.put(id, name);
        });

        return programMap;
    }

    public GetTravelProgramsResponse getTravelPrograms(List<Integer> countries) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("countries", countries != null ? countries.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(":")) : null)
                .addValue("separator", ':'); // Add the list of countries to the map
        List<TravelProgram> programs = namedParameterJdbcTemplate.query(GET_INS_ABROAD_PROGRAMS_BY_COUNTRIES, params,
                (rs, rowNum) -> new TravelProgram(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("liability"),
                        new TravelProgram.Coverages(
                                rs.getDouble("medicine"),
                                rs.getDouble("accident"),
                                rs.getDouble("covid"),
                                rs.getDouble("evacuation"),
                                rs.getDouble("transport"),
                                rs.getDouble("compensation"),
                                rs.getDouble("dentistry"),
                                rs.getDouble("repatriation"),
                                rs.getDouble("search_rescue"),
                                rs.getDouble("third_party_visit"))));
        return new GetTravelProgramsResponse(programs);
    }

    public GetTravelProgramsResponse getTravelProgramsV2(String countriesRaw, Integer activityId) {
        if (!isSite()) {
            throw new AccessDeniedException("Access denied");
        }
        if (activityId == null || activityId < 0) {
            throw new IllegalArgumentException("activityId parameter is required and must be positive");
        }
        if (countriesRaw == null || countriesRaw.trim().isEmpty()) {
            throw new IllegalArgumentException("countries parameter is required and cannot be null");
        }

        List<Integer> countries;
        try {
            countries = Stream.of(countriesRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("countries must contain valid integer values separated by commas");
        }
        if (countries.isEmpty()) {
            throw new IllegalArgumentException("countries list cannot be empty");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("countryIds", countries);
        params.addValue("activity", activityId);
        List<TravelProgram> programs = namedParameterJdbcTemplate.query(
                GET_INS_ABROAD_PROGRAMS_BY_COUNTRIES_V2,
                params,
                (rs, rowNum) -> new TravelProgram(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("liability"),
                        new TravelProgram.Coverages(
                                rs.getDouble("medicine"),
                                rs.getDouble("accident"),
                                rs.getDouble("covid"),
                                rs.getDouble("evacuation"),
                                rs.getDouble("transport"),
                                rs.getDouble("compensation"),
                                rs.getDouble("dentistry"),
                                rs.getDouble("repatriation"),
                                rs.getDouble("search_rescue"),
                                rs.getDouble("third_party_visit")
                        )
                ));
        return new GetTravelProgramsResponse(programs);
    }

    public GetTravelMultiDayTypesResponse getTravelMultiDayTypes(Integer travelTypeId, String language) {
        List<TravelMultiDayType> types = jdbcTemplate.query(getInsAbroadMulti(getLangCode(language)),
                new Object[] { travelTypeId },
                new int[] { Types.INTEGER }, (rs, rowNum) -> new TravelMultiDayType(
                        rs.getInt("id"), rs.getInt("days"), rs.getString("name")));

        return new GetTravelMultiDayTypesResponse(types);
    }

    private int getLangCode(String language) {
        int lang;
        switch (language) {
            case "en" -> lang = 4;
            case "uz" -> lang = 2;
            case "uz-cyrl" -> lang = 3;
            default -> lang = 1;
        }
        return lang;
    }

}
