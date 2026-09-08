create or replace package body FOR_TRAVEL_API_UPDATED is

    procedure set_program_name_to_polis(p_anketa_id in number);

    function CREATE_KONTRAGENT(
        p_is_fiz_yur number,
        p_pinfl varchar2,
        p_pass_sery varchar2,
        p_pass_num varchar2,
        p_first_name varchar2,
        p_last_name varchar2,
        p_middle_name varchar2,
        p_gender number,
        p_birth_date date,
        p_region_id number,
        p_district_id number,
        p_address varchar2,
        p_phone varchar2,
        p_resident_id number,
        p_country_id number,
        p_email varchar2,
        p_org_inn number,
        p_org_name varchar2,
        p_org_oked number,
        p_org_representative_name varchar2,
        p_org_checking_account varchar2,
        p_org_bank_name varchar2,
        p_org_region_id number,
        p_org_district_id number,
        p_org_phone varchar2,
        p_org_address varchar2,
        p_org_email varchar2,
        p_user_id number,
        p_error_code out number,
        p_error_msg out varchar2
    ) RETURN NUMBER is
        v_kont_id       number;
        v_client_cnt    number;
        v_is_local_pass number;
        v_doc_type      VARCHAR2(1024);
        v_doc_status    NUMBER;
        v_date_end      VARCHAR2(30);
    begin
        p_error_code := 0;
        p_error_msg := 'Kontragent created successfully!';

        begin
            select nvl(TB_ID, 0)
            into v_kont_id
            from ins_kontragent
            where TB_INPS = p_pinfl
              and IS_LOCAL_PASSPORT = v_is_local_pass
              and TB_DATEBIRTH = p_birth_date
              and rownum = 1
            order by TB_ID desc;
        exception
            when no_data_found then
                v_kont_id := 0;
        end;

        if v_kont_id = 0 then
            begin
                select nvl(TB_ID, 0)
                into v_kont_id
                from ins_kontragent
                where TB_INPS = p_pinfl
                  and ROWNUM = 1
                order by TB_ID desc;
            exception
                when no_data_found then
                    v_kont_id := 0;
            end;
        end if;

        --for fiz litso or yur litso
        if p_is_fiz_yur = 0 then
            if v_kont_id = 0 then
                select INS_KONTRAGENT_SEQ.nextval
                into v_kont_id
                from dual;

                INSERT INTO INS_KONTRAGENT(TB_ID,
                                           TB_MASTERID,
                                           TB_FIZYUR,
                                           TB_NAME,
                                           TB_SURNAME,
                                           TB_PATRONYM,
                                           TB_INPS,
                                           TB_PASPNUMBER,
                                           TB_PASPSERY,
                                           TB_SEX,
                                           TB_DATEBIRTH,
                                           TB_REZIDENT,
                                           TB_COUNTRY,
                                           TB_OBLAST,
                                           TB_RAYON,
                                           TB_ULICA,
                                           TB_PHONE1,
                                           USER_ID,
                                           MOD_USER,
                                           IS_LOCAL_PASSPORT, tb_email)
                VALUES (v_kont_id,
                        v_kont_id,
                        p_is_fiz_yur,
                        p_first_name,
                        p_last_name,
                        p_middle_name,
                        p_pinfl,
                        p_pass_num,
                        p_pass_sery,
                        p_gender,
                        p_birth_date,
                        p_resident_id,
                        p_country_id,
                        p_region_id,
                        p_district_id,
                        p_address,
                        p_phone,
                        p_user_id,
                        p_user_id,
                        v_is_local_pass, p_email);
            else
                update INS_KONTRAGENT
                set TB_PASPSERY       = p_pass_sery,
                    TB_PASPNUMBER     = p_pass_num,
                    TB_ULICA          = p_address,
                    TB_OBLAST         = p_region_id,
                    TB_RAYON          = p_district_id,
                    TB_PHONE1         = p_phone,
                    TB_DATEBIRTH      = p_birth_date,
                    TB_SURNAME        = p_last_name,
                    TB_NAME           = p_first_name,
                    TB_PATRONYM       = p_middle_name,
                    TB_SEX            = p_gender,
                    IS_LOCAL_PASSPORT = v_is_local_pass,
                    TB_REZIDENT       = p_resident_id,
                    TB_COUNTRY        = p_country_id,
                    tb_email          = p_email
                where TB_ID = v_kont_id;
                commit;
            end if;
            COMMIT;
            return v_kont_id;
        else
            select count(*)
            into v_client_cnt
            from ins_kontragent k
            where k.tb_orginn = p_org_inn
              and k.tb_orgname = p_org_name
              and k.TB_DIREKTOR = p_org_representative_name
              and k.TB_ORGSCHET = p_org_checking_account
              and k.TB_ORGBANK = p_org_bank_name
              and k.TB_KOD_OKONX = p_org_oked
              and k.TB_PHONE1 = p_org_phone;

            if v_client_cnt >= 1 then
                select k.TB_ID
                into v_kont_id
                from ins_kontragent k
                where k.tb_orginn = p_org_inn
                  and k.tb_orgname = p_org_name
                  and k.TB_DIREKTOR = p_org_representative_name
                  and k.TB_ORGSCHET = p_org_checking_account
                  and k.TB_ORGBANK = p_org_bank_name
                  and k.TB_KOD_OKONX = p_org_oked
                  and k.TB_PHONE1 = p_org_phone
                  and ROWNUM = 1
                order by k.TB_ID desc;
                return v_kont_id;
            else
                select INS_KONTRAGENT_SEQ.nextval into v_kont_id from dual;
                INSERT INTO INS_KONTRAGENT(TB_ID,
                                           TB_MASTERID,
                                           TB_FIZYUR,
                                           TB_ORGINN,
                                           TB_ORGNAME,
                                           TB_KOD_OKONX,
                                           TB_DIREKTOR,
                                           TB_ORGSCHET,
                                           TB_ORGBANK,
                                           TB_COUNTRY,
                                           TB_OBLAST,
                                           TB_RAYON,
                                           TB_ULICA,
                                           TB_PHONE1,
                                           USER_ID,
                                           MOD_USER, tb_email)
                VALUES (v_kont_id,
                        v_kont_id,
                        p_is_fiz_yur,
                        p_org_inn,
                        p_org_name,
                        p_org_oked,
                        p_org_representative_name,
                        p_org_checking_account,
                        p_org_bank_name,
                        p_country_id,
                        p_org_region_id,
                        p_org_district_id,
                        p_org_address,
                        p_org_phone,
                        p_user_id,
                        p_user_id, p_org_email);
                commit;
                return v_kont_id;
            end if;
        end if;
    EXCEPTION
        WHEN OTHERS THEN
            p_error_code := 1000;
            p_error_msg := 'FOR_TRAVEL_API.CREATE_KONTRAGENT: ' || DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' ||
                           DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
            ROLLBACK;
            RETURN p_error_code;
    end;

    function calculatePremium(
        travel_type number,
        activity_type number,
        group_type number,
        program_type number,
        days number default 1,
        multi_type number,
        liability out number,
        curs out number,
        out_error_code out number,
        out_error_msg out varchar2
    ) return number is
        v_min        number;
        v_max        number;
        v_days       number;
        v_activity   number;
        v_group      number;
        v_group_type number := group_type;
    begin
        v_days := CASE travel_type
                      WHEN 0 THEN days
                      WHEN 1 THEN 1
                      ELSE days
            END;

        -- get insurance liability based on the program id
        begin
            select OTV
            into liability
            from INS_ABROAD_OTV
            where PROGRAM_ID = program_type
              and ACTIVE = 1;
        exception
            when no_data_found then
                raise_application_error(-20422, 'Wrong program type provided!');
        end;

        select F_INS_GETKURS(2, trunc(sysdate)) into curs from dual;

        v_activity := case
                          when (activity_type = 1 and days < 183) then 1.25
                          when activity_type = 2 then 1.25
                          when activity_type = 5 then 10
                          when activity_type = 6 then 2.00
                          when activity_type = 7 then 2.60
                          when activity_type = 4 then 0.75
                          when activity_type = 3 and travel_type = 0 and days > 182 then 0.75
                          else 1
            end;

        if activity_type != 0 then
            v_group_type := 0;
        end if;

        begin
            select ins_koef
            into v_group
            from ins_abroad_group
            where ins_id = v_group_type;
        exception
            when no_data_found then
                raise_application_error(-20422, 'Wrong group type provided!');
        end;

        select min, max, rate_min
        into v_min, v_max, v_max
        from (select rate_min,
                     RATE_MIN * v_days * v_activity * v_group as min,
                     RATE_MAX * v_days * v_activity * v_group as max
              from INS_ABROAD_RATES
              where PROGRAM_ID = program_type
                and POLIS_TYPE = travel_type
                and (
                  (travel_type = 0 and days between DAYS_FROM and DAYS_TO) or
                  (travel_type = 1 and DAYSELECT = multi_type)
                  ));
        return v_min;

    exception
        when others then
            out_error_code := sqlcode;
            out_error_msg := substr(sqlerrm, 12);
            return -1;
    end;

    function advanced_calculator(
        travel_type number,
        activity_type number,
        group_type number,
        program_type number,
        days number default 1,
        multi_type number,
        liability out number,
        people in out INSURED_PERSON_TRAVEL,
        custom_prem in number,
        curs out number,
        premium_uzs out number,
        out_error_code out number,
        out_error_msg out varchar2
    ) return number is
        result          number;
        coefficient     number;
        age             number;
        prem            number := 0;
        prem_per_person number;
        prem_min        number;
        prem_max        number;
        travelers_count number;
    begin

        result := calculatePremium(travel_type => travel_type,
                                   activity_type => activity_type,
                                   group_type => group_type,
                                   program_type => program_type,
                                   days => days,
                                   multi_type => multi_type,
                                   liability => liability,
                                   curs => curs,
                                   out_error_code => out_error_code,
                                   out_error_msg => out_error_msg);

        if out_error_code <> 0 then
            raise_application_error(out_error_code, out_error_msg);
        end if;
        if custom_prem is not null or custom_prem != 0 then
            result := custom_prem;
        end if;

        for i in 1..people.COUNT
            loop

                age := TRUNC(TRUNC(MONTHS_BETWEEN(SYSDATE, to_date(SUBSTR(people(i).date_birth, 1, 10), 'yyyy-mm-dd'))) / 12);
                if age not between 0 and 100 then
                    raise_application_error(-20422, 'Age is not in the allowed diapason(0, 100)!');
                end if;
                coefficient := 1;

                case
                    when group_type between 2 and 5 then coefficient := 1;
                    else case
                        -- when age <= 24 then if (activity_type = 0 and travel_type = 0) and days < 92 then
                        -- when age <= 24 then                        
                        --     coefficient := 0.8;
                        -- else
                        --     coefficient := 1;
                        -- end if;
                        when age <= 24 then coefficient := 0.8;
                        when age between 25 and 65 then coefficient := 1;
                        when age between 66 and 70 then coefficient := 2;
                        when age between 71 and 75 then coefficient := 3;
                        when age between 76 and 80 then coefficient := 4;
                        when age between 81 and 85 then coefficient := 5;
                        when age >= 86 then coefficient := 10;
                        else coefficient := 1;
                        end case;
                    end case;
                if people(i).IS_ALCOHOL_RISK is not null then
                    if people(i).IS_ALCOHOL_RISK = 1 then
                        coefficient := coefficient * 1.5;
                    end if;
                end if;
                prem_per_person := round(coefficient * result, 2);
                prem := prem + prem_per_person;

                people(i).PREM := prem_per_person;
                people(i).AGE := age;

                select p_min,
                       p_max
                into prem_min, prem_max
                from table (dec_tl_initial_price(ins_day => days,
                                                 tl_group => group_type,
                                                 program => program_type,
                                                 tl_polis_type => travel_type,
                                                 multi_days => multi_type,
                                                 activity => activity_type));
                people(i).PREM_MAX := prem_max;
                people(i).PREM_MIN := prem_min;
                people(i).RATE := coefficient * DEC_ABROAD_ACTIVITY(activity_type);
                people(i).LIABILITY := liability;
            end loop;

        out_error_code := 0;
        out_error_msg := 'Successfully processed';

        premium_uzs := round(prem * curs);
        liability := liability * people.COUNT;
        return prem;

    exception
        when others then
            out_error_code := sqlcode;
            out_error_msg := substr(sqlerrm, 12);
            return -1;
    end;

    function advanced_calculator_(
        travel_type number,
        activity_type number,
        group_type number,
        program_type number,
        days number default 1,
        multi_type number,
        liability out number,
        people in out INSURED_PERSON_TRAVEL,
        custom_prem in number,
        curs out number,
        premium_uzs out number,
        out_error_code out number,
        out_error_msg out varchar2
    ) return number is
        result          number;
        coefficient     number;
        age             number;
        prem            number := 0;
        prem_per_person number;
        prem_min        number;
        prem_max        number;
        travelers_count number;
    begin

        result := calculatePremium(travel_type => travel_type,
                                   activity_type => activity_type,
                                   group_type => group_type,
                                   program_type => program_type,
                                   days => days,
                                   multi_type => multi_type,
                                   liability => liability,
                                   curs => curs,
                                   out_error_code => out_error_code,
                                   out_error_msg => out_error_msg);

        if out_error_code <> 0 then
            raise_application_error(out_error_code, out_error_msg);
        end if;
        if custom_prem is not null or custom_prem != 0 then
            result := custom_prem;
        end if;
        if group_type = 1 then
            SELECT count(*)
            into travelers_count
            FROM APEX_COLLECTIONS
            WHERE COLLECTION_NAME = 'TRAVELERS_COLLECTION_CALCULATION';
            if travelers_count > 0 then
                result := round(result / travelers_count, 2);
            end if;
        end if;

        for i in 1..people.COUNT
            loop

                age := TRUNC(TRUNC(MONTHS_BETWEEN(SYSDATE, to_date(SUBSTR(people(i).date_birth, 1, 10), 'yyyy-mm-dd'))) / 12);
                if age not between 0 and 100 then
                    raise_application_error(-20422, 'Age is not in the allowed diapason(0, 100)!');
                end if;
                coefficient := 1;

                case
                    when group_type between 2 and 5 then coefficient := 1;
                    else case
                        when age <= 24 then if (activity_type = 0 and travel_type = 0) and days < 92 then
                            coefficient := 0.8;
                        else
                            coefficient := 1;
                        end if;
                        when age between 25 and 65 then coefficient := 1;
                        when age between 66 and 70 then coefficient := 2;
                        when age between 71 and 75 then coefficient := 3;
                        when age between 76 and 80 then coefficient := 4;
                        when age between 81 and 85 then coefficient := 5;
                        when age >= 86 then coefficient := 10;
                        else coefficient := 1;
                        end case;
                    end case;
                if people(i).IS_ALCOHOL_RISK is not null then
                    if people(i).IS_ALCOHOL_RISK = 1 then
                        coefficient := coefficient * 1.5;
                    end if;
                end if;
                prem_per_person := round(coefficient * result, 2);
                prem := prem + prem_per_person;

                people(i).PREM := prem_per_person;
                people(i).AGE := age;

                select p_min,
                       p_max
                into prem_min, prem_max
                from table (dec_tl_initial_price(ins_day => days,
                                                 tl_group => group_type,
                                                 program => program_type,
                                                 tl_polis_type => travel_type,
                                                 multi_days => multi_type,
                                                 activity => activity_type));
                people(i).PREM_MAX := prem_max;
                people(i).PREM_MIN := prem_min;
                people(i).RATE := coefficient * DEC_ABROAD_ACTIVITY(activity_type);
                people(i).LIABILITY := liability;
            end loop;

        out_error_code := 0;
        out_error_msg := 'Successfully processed';

        premium_uzs := ceil(prem * curs / 1000) * 1000;
        liability := liability * people.COUNT;
        return prem;

    exception
        when others then
            out_error_code := sqlcode;
            out_error_msg := substr(sqlerrm, 12);
            return -1;
    end;

    function advanced_calculator_fiz(
        travel_type number,
        activity_type number,
        group_type number,
        program_type number,
        days number default 1,
        multi_type number,
        liability out number,
        people in out INSURED_PERSON_TRAVEL,
        curs out number,
        premium_uzs out number,
        out_error_code out number,
        out_error_msg out varchar2
    ) return number is
        result          number;
        coefficient     number;
        age             number;
        prem            number := 0;
        prem_per_person number;
    begin

        result := calculatePremium(travel_type => travel_type,
                                   activity_type => activity_type,
                                   group_type => group_type,
                                   program_type => program_type,
                                   days => days,
                                   multi_type => multi_type,
                                   liability => liability,
                                   curs => curs,
                                   out_error_code => out_error_code,
                                   out_error_msg => out_error_msg);

        if out_error_code <> 0 then
            raise_application_error(-20421, out_error_msg);
        end if;

        for i in 1..people.COUNT
            loop

                age := TRUNC(TRUNC(MONTHS_BETWEEN(SYSDATE, to_date(SUBSTR(people(i).date_birth, 1, 10), 'yyyy-mm-dd'))) / 12);
                if age not between 0 and 100 then
                    raise_application_error(-20422, 'Age is not in the allowed diapason(0, 100)!');
                end if;
                coefficient := 1;

                case
                    when group_type = 1 then coefficient := 1;
                                             if i <> 1 then
                                                 prem := 0;
                                             end if;
                    when group_type between 2 and 5 then coefficient := 1;
                    else case
                        -- when age <= 24 then if (activity_type = 0 and travel_type = 0) and days < 92 then
                        -- when age <= 24 then                        
                        --     coefficient := 0.8;
                        -- else
                        --     coefficient := 1;
                        -- end if;
                        when age <= 24 then coefficient := 0.8;
                        when age between 24 and 65 then coefficient := 1;
                        when age between 66 and 70 then coefficient := 2;
                        when age between 71 and 75 then coefficient := 3;
                        when age between 76 and 80 then coefficient := 4;
                        when age between 81 and 85 then coefficient := 5;
                        when age >= 86 then coefficient := 10;
                        else coefficient := 1;
                        end case;
                    end case;
                prem_per_person := coefficient * result;
                prem := prem + prem_per_person;

                people(i).PREM := prem_per_person;
                people(i).AGE := age;
                people(i).PREM_MAX := prem_per_person;
                people(i).PREM_MIN := prem_per_person;
                people(i).RATE := coefficient;
                people(i).LIABILITY := liability;
            end loop;

        out_error_code := 0;
        out_error_msg := 'Successfully processed';

--         premium_uzs := round(prem * curs, 2);
        premium_uzs := round(prem * curs);
        liability := liability * people.COUNT;
        return prem;

    exception
        when others then
            out_error_code := sqlcode;
            out_error_msg := substr(sqlerrm, 12);
            return -1;
    end advanced_calculator_fiz;

    PROCEDURE Get_Program_By_Country(res OUT travel_programs, countries VARCHAR2) is
        separator char(1) := ':';
        v_Sql     VARCHAR2(4000);
    BEGIN

        IF length(countries) > 0 THEN

            v_Sql := 'select INS_ID as id, PROGRAM_NAME
              from INS_ABROAD_PROGRAM
             where (INS_ID = 1 and
                    (select min(pr1)
                       from sp_country
                      where fond_id in (select COLUMN_VALUE from table (split(:countries, :separator)))
                    ) = 1 or :countries is null
                 )
                or (INS_ID = 2 and
                    (select min(pr2)
                       from sp_country
                      where fond_id in (select COLUMN_VALUE from table (split(:countries, :separator)))
                    ) = 1 or :countries is null
                 )
                or (INS_ID = 3 and
                    (select min(pr3)
                       from sp_country
                      where fond_id in (select COLUMN_VALUE from table (split(:countries, :separator)))
                    ) = 1 or :countries is null
                 )
                or (INS_ID = 4 and
                    (select min(pr4)
                       from sp_country
                      where fond_id in (select COLUMN_VALUE from table (split(:countries, :separator)))
                    ) = 1 or :countries is null
                 )
                or (INS_ID = 5 and
                    (select min(pr5)
                       from sp_country
                      where fond_id in (select COLUMN_VALUE from table (split(:countries, :separator)))
                    ) = 1 or :countries is null
                 )
                or (INS_ID = 6 and
                    (select min(pr6)
                       from sp_country
                      where fond_id in (select COLUMN_VALUE from table (split(:countries, :separator)))
                    ) = 1 or :countries is null
                       ) and ACTIVE = 1
             order by 1';
            EXECUTE IMMEDIATE v_Sql BULK COLLECT INTO res USING countries, separator;
        END IF;
    END;

    function CREATE_CONTRACT(
        startDate date,
        days number,
        travel_type number,
        multiId number,
        programId number,
        activityId number,
        groupId number,
        countries varchar2,
        app_Fiz_yur number,
        app_Date_Birth date,
        app_Pass_Series varchar2,
        app_Pass_Num varchar2,
        app_Pass_given_date date,
        app_pass_given_by varchar2,
        app_Pinfl varchar2,
        app_Last_Name varchar2,
        app_First_Name varchar2,
        app_Middle_Name varchar2,
        app_Address varchar2,
        app_Phone varchar2,
        app_Rezident number,
        app_district number,
        app_region number,
        app_country number,
        app_Gender number,
        app_Email varchar2,
        app_Org_Rezident number,
        app_Inn varchar2,
        app_Org_Name varchar2,
        app_Org_Oked varchar2,
        app_Org_Representative_Name varchar2,
        app_Org_Checking_Account varchar2,
        app_Org_Bank varchar2,
        app_Org_Region_Id number,
        app_Org_District_Id number,
        app_Org_Phone varchar2,
        app_Org_Address varchar2,
        app_Org_Email varchar2,
        userId number,
        people INSURED_PERSON_TRAVEL,
        out_error out number,
        out_error_text out varchar2
    ) RETURN number is
        v_contract_id         number;
        v_date_reg            date   := trunc(sysdate);
        v_ins_div             number;
        v_val_kurs            number;
        v_kurs_otv            number;
        v_owner               number;
        v_prem_usd            number := 0;
        v_prem_uzs            number;
        v_prem_uzs_all        number := 0;
        v_end_date            date;
        v_liability           number;
        v_days                number;
        insured_people        INSURED_PERSON_TRAVEL;
        local_countries       varchar2(200);
        travel_id             number;
        cnt                   number := 0;
        sumOTV                number;
        sumPREM               number;
        sumOTV_SUM            number;
        v_shengen             number;
        commision             number;
        sogl                  number;
        object_count          number := people.COUNT;
        v_client_id           number;
        v_doc_num             number;
        v_temp_dis_id         number;
        v_temp_reg_id         number;
        v_doc_type            VARCHAR2(1024);
        v_doc_status          NUMBER;
        v_date_end            VARCHAR2(30);
        v_is_agent            number;
        v_ins_rate            number;
        v_commission_sum      number := 0;
        v_insurance_comission number := 0;
    BEGIN

        out_error := 0;
        out_error_text := 'Successfully completed!';

        insured_people := people;
        if travel_type = 0 then
            v_end_date := startDate + days - 1;
        else
            begin
                select ins_days
                into v_days
                from INS_ABROAD_MULTI
                where multiId = INS_ID;
            exception
                when no_data_found then
                    raise_application_error(-20422, 'Multi days provided is wrong!');
            end;
            v_end_date := startDate + v_days;
        end if;

        SELECT Ins_Anketa_Seq.NEXTVAL INTO v_contract_id FROM Dual;


        v_ins_div := getuserdiv(userId);

        v_kurs_otv := F_INS_GETKURS(3, v_date_reg);

        /*v_owner := FOR_BANK_API.Insert_Kontragent(V_PINFL => app_Pinfl,
                                                  V_PASS_SERY => app_Pass_Series,
                                                  V_PASS_NUM => app_Pass_Num,
                                                  V_SURNAME => app_Last_Name,
                                                  V_GIVEN_NAME => app_First_Name,
                                                  V_PATRONYMIC => app_Middle_Name,
                                                  V_BIRTH_DAY => app_Date_Birth,

                                                  V_PASS_GIVEN => app_pass_given_by,
                                                  V_PASS_GIVEN_DATE => app_Pass_given_date,

                                                  V_REGION => app_region,
                                                  V_DISTRICT => app_district,
                                                  V_ADDRESS => app_Address,
                                                  V_PHONE => app_Phone,
                                                  V_USER_ID => userId,
                                                  V_FIZ_YUR => app_Fiz_yur,
                                                  V_INN => app_Inn,
                                                  V_ORGNAME => app_Org_Name,
                                                  V_ORGMFO => null);*/

        if app_Fiz_yur = 0 then
            v_owner := CREATE_KONTRAGENT(p_is_fiz_yur => app_Fiz_yur,
                                         p_pinfl => app_Pinfl,
                                         p_pass_sery => app_Pass_Series,
                                         p_pass_num => app_Pass_Num,
                                         p_first_name => app_First_Name,
                                         p_last_name => app_Last_Name,
                                         p_middle_name => app_Middle_Name,
                                         p_gender => app_Gender,
                                         p_birth_date => app_Date_Birth,
                                         p_region_id => app_region,
                                         p_district_id => app_district,
                                         p_address => app_Address,
                                         p_phone => app_Phone,
                                         p_resident_id => app_Rezident,
                                         p_country_id => app_country,
                                         p_email => app_Email,
                                         p_org_inn => null,
                                         p_org_name => null,
                                         p_org_oked => null,
                                         p_org_representative_name => null,
                                         p_org_checking_account => null,
                                         p_org_bank_name => null,
                                         p_org_region_id => null,
                                         p_org_district_id => null,
                                         p_org_phone => null,
                                         p_org_address => null,
                                         p_org_email => app_Org_Email,
                                         p_user_id => userId,
                                         p_error_code => out_error,
                                         p_error_msg => out_error_text);
            if out_error != 0 then
                rollback;
                return out_error;
            end if;

            UPDATE INS_KONTRAGENT
            SET TB_SEX = app_gender
            WHERE TB_ID = v_owner;
            commit;
        else
            v_owner := CREATE_KONTRAGENT(p_is_fiz_yur => app_Fiz_yur,
                                         p_pinfl => null,
                                         p_pass_sery => null,
                                         p_pass_num => null,
                                         p_first_name => null,
                                         p_last_name => null,
                                         p_middle_name => null,
                                         p_gender => null,
                                         p_birth_date => null,
                                         p_region_id => null,
                                         p_district_id => null,
                                         p_address => null,
                                         p_phone => null,
                                         p_resident_id => app_Org_Rezident,
                                         p_country_id => null,
                                         p_email => null,
                                         p_org_inn => app_Inn,
                                         p_org_name => app_Org_Name,
                                         p_org_oked => app_Org_Oked,
                                         p_org_representative_name => app_Org_Representative_Name,
                                         p_org_checking_account => app_Org_Checking_Account,
                                         p_org_bank_name => app_Org_Bank,
                                         p_org_region_id => app_Org_Region_Id,
                                         p_org_district_id => app_Org_District_Id,
                                         p_org_phone => app_Org_Phone,
                                         p_org_address => app_Org_Address,
                                         p_org_email => app_Org_Email,
                                         p_user_id => userId,
                                         p_error_code => out_error,
                                         p_error_msg => out_error_text);
            if out_error != 0 then
                rollback;
                return out_error;
            end if;
        end if;

        if v_owner = -1 then
            raise_application_error(-20404, 'Insurant did not saved!');
        end if;

        --         v_prem_usd := advanced_calculator(travel_type => travel_type,
--                                           activity_type => activityId,
--                                           group_type => groupId,
--                                           program_type => programId,
--                                           days => days,
--                                           multi_type => multiId,
--                                           liability => v_liability,
--                                           people => insured_people,
--                                           custom_prem => null,
--                                           curs => v_val_kurs,
--                                           premium_uzs => v_prem_uzs,
--                                           out_error_code => out_error,
--                                           out_error_msg => out_error_text);

        select SUM(NVL(TO_NUMBER(C017), 0)), SUM(NVL(TO_NUMBER(C018), 0))
        INTO v_prem_usd, v_prem_uzs_all
        FROM APEX_COLLECTIONS
        WHERE COLLECTION_NAME = 'TRAVELERS_COLLECTION';

        select F_INS_GETKURS(2, trunc(sysdate)) into v_val_kurs from dual;

        IF out_error != 0 THEN
            ROLLBACK;
            RETURN out_error;
        end if;

        if v_prem_usd < 0 then
            return -1;
        end if;

        select max(SHENGEN)
        into v_shengen
        from sp_country
        where SP_ID in (select COLUMN_VALUE from table (split(countries, ':')));
        SELECT Sq_Tb_Polis.Nextval INTO v_doc_num FROM Dual;

        INSERT INTO ins_anketa
        (INS_ID,
         INS_DIV,
         INS_TYPE,
         OWNER,
         VAL_TYPE,
         VAL_KURS,
         VAL_USLOVIYA,
         POLIS_POOBYEKTAM,
         POLIS_POOPLATE,
         USLOVIE_OPLATI,
         USER_ID,
         INS_DATE_OSGOR,
         KURS_OTV,
         ABROAD_MIN,
         ABROAD_MAX,
         INS_DATE,
         ABROAD_POLIS_TYPE,
         INS_DATEF,
         INS_DATET,
         INS_DAY,
         ABROAD_DAYS,
         ABROAD_COUNTRY,
         ABROAD_PROGRAM,
         ABROAD_ACTIVITY,
         ABROAD_GROUP,
         ABROAD_PREM, OWN_NAME, own_address, own_phone, BENEFICIARY, FIZYUR, INS_DOGNUM)
        VALUES (v_contract_id,
                v_ins_div,
                17,
                v_owner,
                2,
                v_val_kurs,
                3,
                0,
                0,
                0,
                userId,
                v_date_reg,
                v_kurs_otv,
                v_prem_usd,
                v_prem_usd,
                v_date_reg,
                travel_type,
                startDate,
                v_end_date,
                days,
                multiId,
                countries,
                programId,
                activityId,
                groupId,
                v_prem_usd, app_Last_Name || ' ' || app_First_Name || ' ' || app_Middle_Name, app_Address, app_Phone,
                v_owner, app_Fiz_yur, v_doc_num);
        COMMIT;

        select round(case
                         when A."INS_OTV" = 0 or A.INS_DAY = 0 then
                             null
                         else
                             A."INS_PREM" / A."INS_OTV" * 36500 /
                             (A.INS_DAY)
                         end,
                     4)
        into v_ins_rate
        from INS_ANKETA A
        where A.INS_ID = v_contract_id;

        --saving travellers from APEX_COLLECTIONS to INS_TRAVEL STARTED
        if people.COUNT = 0 then
            IF APEX_COLLECTION.COLLECTION_EXISTS('TRAVELERS_COLLECTION') THEN
                FOR v_travellers_obj IN (SELECT * FROM APEX_COLLECTIONS WHERE COLLECTION_NAME = 'TRAVELERS_COLLECTION')
                    LOOP
                    --check passport date is valid or not started?
--                         SELECT DOCTYPE,
--                                DATEEND,
--                                DOCSTATUS
--                         INTO v_doc_type,
--                             v_date_end,
--                             v_doc_status
--                         FROM TABLE (GET_DOCUMENT_DETAILS(v_travellers_obj.C004,
--                                                          v_travellers_obj.C002,
--                                                          v_travellers_obj.C003))
--                         where rownum = 1;
--
--                         if v_doc_type = 'IDMS_RECV_IP_DOCUMENTS' then
--                             -- v_pass_is_local = 2;
--                             if to_date(v_date_end, 'DD.MM.YYYY') is not null then
--                                 if to_date(v_date_end, 'DD.MM.YYYY') < sysdate AND v_doc_status != 2 then
--                                     out_error := 2222;
--                                     out_error_text := v_travellers_obj.C002 || ' ' || v_travellers_obj.C003;
--                                     return out_error;
--                                 end if;
--                             end if;
--                         elsif v_doc_type = 'IDMS_RECV_CITIZ_DOCUMENTS' then
--                             -- v_pass_is_local = 1;
--                             if to_date(v_date_end, 'DD.MM.YYYY') is not null then
--                                 if to_date(v_date_end, 'DD.MM.YYYY') < sysdate and v_doc_status != 2 then
--                                     out_error := 1111;
--                                     out_error_text := v_travellers_obj.C002 || ' ' || v_travellers_obj.C003;
--                                     return out_error;
--                                 end if;
--                             end if;
--                         elsif v_doc_type = 'IDMS_RECV_MVD_IDCARD_CITIZEN' then
--                             -- v_pass_is_local = 3;
--                             if to_date(v_date_end, 'DD.MM.YYYY') is not null then
--                                 if to_date(v_date_end, 'DD.MM.YYYY') < sysdate and v_doc_status != 2 then
--                                     out_error := 3333;
--                                     out_error_text := v_travellers_obj.C002 || ' ' || v_travellers_obj.C003;
--                                     return out_error;
--                                 end if;
--                             end if;
--                             -- else
--                             --     out_error := 4444;
--                             --     out_error_text := v_travellers_obj.C002 || ' ' || v_travellers_obj.C003;
--                             --     return out_error;
--                         end if;
                    --check passport date is valid or not ended

                    /*v_client_id := FOR_TRAVEL_API_UPDATED.INSERT_TRAVELER(V_PINFL => v_travellers_obj.C004,
                                                                          V_PASS_SERY => v_travellers_obj.C002,
                                                                          V_PASS_NUM => v_travellers_obj.C003,
                                                                          V_SURNAME => v_travellers_obj.C005,
                                                                          V_GIVEN_NAME => v_travellers_obj.C006,
                                                                          V_PATRONYMIC => v_travellers_obj.C007,
                                                                          V_BIRTH_DAY => v_travellers_obj.C001,
                                                                          V_PASS_GIVEN => null,
                                                                          V_PASS_GIVEN_DATE => null,
                                                                          V_REGION => v_travellers_obj.C009,
                                                                          V_DISTRICT => v_travellers_obj.C010,
                                                                          V_ADDRESS => v_travellers_obj.C011,
                                                                          V_USER_ID => userId,
                                                                          error_text => out_error_text,
                                                                          error_code => out_error);*/

                        v_client_id := CREATE_KONTRAGENT(p_is_fiz_yur => 0,
                                                         p_pinfl => v_travellers_obj.C004,
                                                         p_pass_sery => v_travellers_obj.C002,
                                                         p_pass_num => v_travellers_obj.C003,
                                                         p_first_name => v_travellers_obj.C006,
                                                         p_last_name => v_travellers_obj.C005,
                                                         p_middle_name => v_travellers_obj.C007,
                                                         p_gender => v_travellers_obj.C023,
                                                         p_birth_date => v_travellers_obj.C001,
                                                         p_region_id => v_travellers_obj.C009,
                                                         p_district_id => v_travellers_obj.C010,
                                                         p_address => v_travellers_obj.C011,
                                                         p_phone => v_travellers_obj.C012,
                                                         p_resident_id => v_travellers_obj.C022,
                                                         p_country_id => v_travellers_obj.C008,
                                                         p_email => v_travellers_obj.C030,
                                                         p_org_inn => null,
                                                         p_org_name => null,
                                                         p_org_oked => null,
                                                         p_org_representative_name => null,
                                                         p_org_checking_account => null,
                                                         p_org_bank_name => null,
                                                         p_org_region_id => null,
                                                         p_org_district_id => null,
                                                         p_org_phone => null,
                                                         p_org_address => null,
                                                         p_org_email => null,
                                                         p_user_id => userId,
                                                         p_error_code => out_error,
                                                         p_error_msg => out_error_text);
                        if out_error != 0 then
                            ROLLBACK;
                            return out_error;
                        end if;

                        UPDATE INS_KONTRAGENT
                        SET TB_SEX = app_gender
                        WHERE TB_ID = v_owner;
                        commit;

                        select INS_TRAVEL_SEQ.NEXTVAL into travel_id from dual;
                        INSERT INTO INS_TRAVEL
                        (INS_ID,
                         ANKETA_ID,
                         OTV,
                         PREM,
                         PREM_MIN,
                         PREM_MAX,
                         YEARS,
                         KOEF,
                         SHENGEN,
                         CLIENT_ID,
                         IS_ALCOHOL_RISK)
                        VALUES (travel_id,
                                v_contract_id,
                                v_travellers_obj.C020,
                                v_travellers_obj.C018,
                                v_travellers_obj.C015,
                                v_travellers_obj.C016,
                                v_travellers_obj.C013,
                                v_travellers_obj.C014,
                                v_shengen,
                                v_client_id,
                                v_travellers_obj.C021);

                        select count(1)
                        into cnt
                        from INS_PSUGURTA_PO_OBYEKTAM
                        where ANKETA_ID = v_contract_id
                          AND AVTO_ID = travel_id;

                        if cnt = 0 then
                            INSERT INTO INS_PSUGURTA_PO_OBYEKTAM (INS_ID,
                                                                  ANKETA_ID,
                                                                  AVTO_ID,
                                                                  LINK_ID,
                                                                  PTURI_ID,
                                                                  DIVISION_ID,
                                                                  VAL_TYPE,
                                                                  VAL_DATE,
                                                                  VAL_KURS,
                                                                  MUKOFOT_F,
                                                                  MUKOFOT,
                                                                  MAJBURIYAT,
                                                                  OBEKT_SONI,
                                                                  MUKOFOT2)
                            VALUES (INS_PSUGURTA_PO_OBYEKTAM_SEQ.nextval,
                                    v_contract_id,
                                    travel_id,
                                    3,
                                    17,
                                    v_ins_div,
                                    2,
                                    v_date_reg,
                                    v_val_kurs,
                                    v_ins_rate,
                                    v_travellers_obj.C017,
                                    v_travellers_obj.C020,
                                    1,
                                    v_travellers_obj.C018);
                            COMMIT;
                        else
                            update INS_PSUGURTA_PO_OBYEKTAM
                            set VAL_DATE   = v_date_reg,
                                VAL_KURS   = v_val_kurs,
                                MUKOFOT    = v_prem_usd,
                                MUKOFOT2   = v_prem_uzs,
                                MUKOFOT_F  = v_ins_rate,
                                MAJBURIYAT = v_liability
                            where ANKETA_ID = v_contract_id
                              and AVTO_ID = travel_id
                              and LINK_ID = 3;
                            COMMIT;
                        end if;

                        FOR rw IN (select po.LINK_ID                      as
                                              LINK_ID,
                                          sum(po.MAJBURIYAT)              as
                                              MAJBURIYAT,
                                          sum(po.MUKOFOT)                 as
                                              MUKOFOT,
                                          sum(po.OBEKT_SONI)              as
                                              OBEKT_SONI,
                                          sum(po.FRAN_SM)                 as
                                              FRAN_SM,
                                          sum(po.MAJBURIYAT * v_val_kurs) as
                                              MAJBURIYAT_SUM
                                   from INS_PSUGURTA_PO_OBYEKTAM po
                                   where po.ANKETA_ID = v_contract_id
                                   group by po.LINK_ID
                                   order by po.LINK_ID)
                            LOOP
                                select count(1)
                                INTO cnt
                                from INS_PSUGURTA
                                where ANKETA_ID = v_contract_id
                                  and LINK_ID = rw.LINK_ID;

                                IF
                                    cnt > 0 THEN
                                    UPDATE INS_PSUGURTA
                                    SET MAJBURIYAT=rw.MAJBURIYAT,
                                        MUKOFOT=rw.MUKOFOT,
                                        OBEKT_SONI=rw.OBEKT_SONI,
                                        FRAN_SM=rw.FRAN_SM,
                                        VAL_TYPE = 2,
                                        MUKOFOT_F = v_ins_rate,
                                        VAL_DATE = v_date_reg,
                                        VAL_KURS = v_val_kurs,
                                        MAJBURIYAT_SUM = rw.MAJBURIYAT_SUM
                                    where ANKETA_ID = v_contract_id
                                      and LINK_ID = rw.LINK_ID;
                                    COMMIT;
                                ELSE
                                    INSERT INTO INS_PSUGURTA (INS_ID,
                                                              ANKETA_ID,
                                                              PTURI_ID,
                                                              LINK_ID,
                                                              MAJBURIYAT,
                                                              MUKOFOT,
                                                              OBEKT_SONI,
                                                              FRAN_SM,
                                                              DIVISION_ID,
                                                              VAL_TYPE,
                                                              VAL_DATE,
                                                              VAL_KURS,
                                                              MAJBURIYAT_SUM,
                                                              MUKOFOT_F)
                                    VALUES (INS_PSUGURTA_SEQ.nextval,
                                            v_contract_id,
                                            17,
                                            rw.LINK_ID,
                                            rw.MAJBURIYAT,
                                            rw.MUKOFOT,
                                            rw.OBEKT_SONI,
                                            rw.FRAN_SM,
                                            v_ins_div,
                                            2,
                                            v_date_reg,
                                            v_val_kurs,
                                            rw.MAJBURIYAT_SUM,
                                            v_ins_rate);
                                    COMMIT;
                                END IF;
                            END LOOP;
                    END LOOP;
                APEX_COLLECTION.DELETE_COLLECTION(p_collection_name => 'TRAVELERS_COLLECTION');
            END IF;
        END IF;
        COMMIT;
        --saving travellers from APEX_COLLECTIONS to INS_TRAVEL ENDED

        FOR i in 1..insured_people.COUNT
            LOOP
                v_client_id := FOR_TRAVEL_API_UPDATED.INSERT_TRAVELER(V_PINFL => insured_people(i).PINFL,
                                                                      V_PASS_SERY => insured_people(i).PASS_SERIES,
                                                                      V_PASS_NUM => insured_people(i).PASS_NUMBER,
                                                                      V_SURNAME => insured_people(i).LAST_NAME,
                                                                      V_GIVEN_NAME => insured_people(i).FIRST_NAME,
                                                                      V_PATRONYMIC => insured_people(i).MIDDLE_NAME,
                                                                      V_BIRTH_DAY => insured_people(i).DATE_BIRTH,
                                                                      V_PASS_GIVEN => insured_people(i).PASS_GIVEN_BY,
                                                                      V_PASS_GIVEN_DATE => insured_people(i).PASS_GIVEN_DATE,
                                                                      v_resident => 1,
                                                                      v_citizenship => 182,
                                                                      v_phone => app_Phone,
                                                                      v_gender => 1,
                                                                      V_REGION => insured_people(i).REGION_ID,
                                                                      V_DISTRICT => insured_people(i).DISTRICT_ID,
                                                                      V_ADDRESS => insured_people(i).ADDRESS,
                                                                      V_USER_ID => userId,
                                                                      error_text => out_error_text,
                                                                      error_code => out_error);
                if out_error != 0 then
                    ROLLBACK;
                    return out_error;
                end if;
                select INS_TRAVEL_SEQ.NEXTVAL into travel_id from dual;
                INSERT INTO INS_TRAVEL
                (INS_ID,
                 ANKETA_ID,
                 OTV,
                 PREM,
                 PREM_MIN,
                 PREM_MAX,
                 YEARS,
                 KOEF,
                 SHENGEN,
                 CLIENT_ID)
                VALUES (travel_id,
                        v_contract_id,
                        insured_people(i).LIABILITY,
                        insured_people(i).PREM,
                        insured_people(i).PREM_MIN,
                        insured_people(i).PREM_MAX,
                        insured_people(i).AGE,
                        insured_people(i).RATE,
                        v_shengen,
                        v_client_id);

                --************************************INS_PSUGURTA_PO_OBYEKTAM******************************************
                select count(1)
                into cnt
                from INS_PSUGURTA_PO_OBYEKTAM
                where ANKETA_ID = v_contract_id
                  AND AVTO_ID = travel_id;

                if cnt = 0 then
                    INSERT INTO INS_PSUGURTA_PO_OBYEKTAM (INS_ID,
                                                          ANKETA_ID,
                                                          AVTO_ID,
                                                          LINK_ID,
                                                          PTURI_ID,
                                                          DIVISION_ID,
                                                          VAL_TYPE,
                                                          VAL_DATE,
                                                          VAL_KURS,
                                                          MUKOFOT_F,
                                                          MUKOFOT,
                                                          MAJBURIYAT,
                                                          OBEKT_SONI,
                                                          MUKOFOT2)
                    VALUES (INS_PSUGURTA_PO_OBYEKTAM_SEQ.nextval,
                            v_contract_id,
                            travel_id,
                            3,
                            17,
                            v_ins_div,
                            2,
                            v_date_reg,
                            v_val_kurs,
                            v_ins_rate,
                            insured_people(i).PREM,
                            insured_people(i).LIABILITY,
                            1,
                            round(insured_people(i).PREM * v_val_kurs));
                    COMMIT;
                else
                    update INS_PSUGURTA_PO_OBYEKTAM
                    set VAL_DATE   = v_date_reg,
                        VAL_KURS   = v_val_kurs,
                        MUKOFOT    = v_prem_usd,
                        MUKOFOT2   = v_prem_uzs,
                        MUKOFOT_F  = v_ins_rate,
                        MAJBURIYAT = v_liability
                    where ANKETA_ID = v_contract_id
                      and AVTO_ID = travel_id
                      and LINK_ID = 3;
                    COMMIT;
                end if;


                --************************************INS_PSUGURTA******************************************
                FOR rw IN (select po.LINK_ID                      as
                                      LINK_ID,
                                  sum(po.MAJBURIYAT)              as
                                      MAJBURIYAT,
                                  sum(po.MUKOFOT)                 as
                                      MUKOFOT,
                                  sum(po.OBEKT_SONI)              as
                                      OBEKT_SONI,
                                  sum(po.FRAN_SM)                 as
                                      FRAN_SM,
                                  sum(po.MAJBURIYAT * v_val_kurs) as
                                      MAJBURIYAT_SUM
                           from INS_PSUGURTA_PO_OBYEKTAM po
                           where po.ANKETA_ID = v_contract_id
                           group by po.LINK_ID
                           order by po.LINK_ID)
                    LOOP
                        select count(1)
                        INTO cnt
                        from INS_PSUGURTA
                        where ANKETA_ID = v_contract_id
                          and LINK_ID = rw.LINK_ID;

                        IF
                            cnt > 0 THEN
                            UPDATE INS_PSUGURTA
                            SET MAJBURIYAT=rw.MAJBURIYAT,
                                MUKOFOT=rw.MUKOFOT,
                                OBEKT_SONI=rw.OBEKT_SONI,
                                FRAN_SM=rw.FRAN_SM,
                                VAL_TYPE = 2,
                                VAL_DATE = v_date_reg,
                                VAL_KURS = v_val_kurs,
                                MAJBURIYAT_SUM = rw.MAJBURIYAT_SUM,
                                MUKOFOT_F = v_ins_rate
                            where ANKETA_ID = v_contract_id
                              and LINK_ID = rw.LINK_ID;
                            COMMIT;
                        ELSE
                            INSERT INTO INS_PSUGURTA (INS_ID,
                                                      ANKETA_ID,
                                                      PTURI_ID,
                                                      LINK_ID,
                                                      MAJBURIYAT,
                                                      MUKOFOT,
                                                      OBEKT_SONI,
                                                      FRAN_SM,
                                                      DIVISION_ID,
                                                      VAL_TYPE,
                                                      VAL_DATE,
                                                      VAL_KURS,
                                                      MAJBURIYAT_SUM, MUKOFOT_F)
                            VALUES (INS_PSUGURTA_SEQ.nextval,
                                    v_contract_id,
                                    17,
                                    rw.LINK_ID,
                                    rw.MAJBURIYAT,
                                    rw.MUKOFOT,
                                    rw.OBEKT_SONI,
                                    rw.FRAN_SM,
                                    v_ins_div,
                                    2,
                                    v_date_reg,
                                    v_val_kurs,
                                    rw.MAJBURIYAT_SUM, v_ins_rate);
                            COMMIT;
                        END IF;
                    END LOOP;

            END LOOP;
        COMMIT;

        -- get insurance liability based on the program id


        --************************************INS_ANKETA******************************************
        SELECT sum(MAJBURIYAT), sum(MUKOFOT), sum(MAJBURIYAT_SUM)
        INTO sumOTV, sumPREM, sumOTV_SUM
        FROM INS_PSUGURTA
        WHERE ANKETA_ID = v_contract_id;


        UPDATE INS_ANKETA
        SET INS_OTV      = sumOTV,
            INS_OTV_SUM  = sumOTV_SUM,
            INS_PREM     = sumPREM,
            INS_PROGRESS = null,
            ins_country  = dec_countrylist(local_countries, 1)
        WHERE INS_ID = v_contract_id;

        select round(case
                         when A."INS_OTV" = 0 or A.INS_DAY = 0 then
                             null
                         else
                             A."INS_PREM" / A."INS_OTV" * 36500 /
                             (A.INS_DAY)
                         end,
                     4)
        into v_ins_rate
        from INS_ANKETA A
        where A.INS_ID = v_contract_id;

        update INS_PSUGURTA_PO_OBYEKTAM set MUKOFOT_F=v_ins_rate where ANKETA_ID = v_contract_id;

        COMMIT;

        -- ******************* INS_OPLATA *********************
        if v_prem_usd > 0 and v_val_kurs > 0 then
            select count(*) into cnt from INS_OPLATA where ANKETA_ID = v_contract_id;

            select IS_AGENT into v_is_agent from TB_USERS where TB_ID = userId;

            commision := F_INS_GETKOMMIS(userId, 17, v_date_reg);
            sogl := F_INS_GETSOGL(userId, v_date_reg);


            if cnt = 0 then
                select sum(MUKOFOT2) * commision / 100
                into v_commission_sum
                from INS_PSUGURTA_PO_OBYEKTAM
                where ANKETA_ID = v_contract_id;

                select round(INS_PREM * VAL_KURS) * commision / 100
                into v_insurance_comission
                from INS_ANKETA
                where INS_ID = v_contract_id;
                insert into INS_OPLATA (INS_ID,
                                        ANKETA_ID,
                                        DIVISION_ID,
                                        USER_ID,
                                        OPL_DATA,
                                        OPL_SUMMA,
                                        INS_TYPE,
                                        OPL_TYPE,
                                        VAL_TYPE,
                                        STATUS,
                                        VAL_KURS,
                                        OPLATA,
                                        OPL_VAL,
                                        KOMMIS_F,
                                        KOMMIS_SUMMA,
                                        SOGL_ID)
                values (INS_OPLATA_SEQ.nextval,
                        v_contract_id,
                        v_ins_div,
                        userId,
                        v_date_reg,
                        ceil(v_prem_uzs_all / 1000) * 1000,
                        17,
                        3,
                        2,
                        0,
                        v_val_kurs,
                        ceil(v_prem_uzs_all / 1000) * 1000,
                        1,
                        commision,
                        case
                            when v_insurance_comission <= v_commission_sum then v_insurance_comission
                            else v_commission_sum end,
                        sogl);
                COMMIT;
            else
                update ins_oplata
                set OPL_DATA  = v_date_reg,
                    OPL_SUMMA = v_prem_uzs_all,
                    VAL_KURS  = v_val_kurs,
                    OPLATA    = v_prem_uzs_all,
                    OPL_TYPE  = 1
                where ANKETA_ID = v_contract_id;
                COMMIT;
            end if;

        end if; --ankprem>0

        return v_contract_id;
    exception
        when others THEN
            OUT_ERROR := sqlcode;
            ASBT_SYS.INS_ERR('Travel error: ' || dbms_utility.format_error_backtrace ||
                             dbms_utility.format_error_stack);
            OUT_ERROR_TEXT := DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
            return -1;
    END CREATE_CONTRACT;


    function Generate_Policy(
        contract_id in number,
        policy_series out varchar2,
        policy_number out number,
        error out number,
        error_text out varchar2,
        policy_uuid out varchar2
    ) RETURN number is
        rPSUGURTA        INS_PSUGURTA%ROWTYPE;
        v_division       number;
        v_val_type       number;
        v_val_kurs       number;
        psumma           number;
        pprem            number;
        pfran            number;
        payment_id       number;
        payment_date     date;
        v_date_begin     date;
        v_date_end       date;
        ankPrem          number;
        kurs             number;
        policy_id        number;
        shn              number := 0;
        days             number;
        policy_count     number;
        phone_number     varchar2(40);
        V_SMS_TEXT       varchar2(4000);
        v_result         varchar2(32000);
        v_user_id        number;
        v_is_online_pym  number;
        v_payment_type   number;
        v_payment_status number;
        v_oplata         number;


        v_v2_check       number;

    begin

        -- === НАЧАЛО ИЗМЕНЕНИЙ: ПРОКСИ-БЛОК ДЛЯ V2 ===
        select count(*) into v_v2_check 
        from INS_POLIS 
        where TB_ANKETA = contract_id;

        if v_v2_check > 0 then
            -- Если у анкеты УЖЕ есть полис (черновик от CREATE_CONTRACT_FIZ_V2),
            -- просто пробрасываем вызов в V2, чтобы избежать дублирования
            return Generate_Policy_v2(contract_id, policy_series, policy_number, error, error_text, policy_uuid);
        end if;
        -- === КОНЕЦ ИЗМЕНЕНИЙ ===

        error := 0;
        error_text := 'Completed successfully';

        begin
            select nvl(INS_PREM, 0),
                   nvl(VAL_KURS, f_ins_getkurs(VAL_TYPE, INS_DATE)),
                   INS_DAY,
                   VAL_KURS,
                   VAL_TYPE,
                   ins_datef,
                   ins_datet,
                   USER_ID
            into ankprem, kurs, days, v_val_kurs, v_val_type, v_date_begin, v_date_end, v_user_id
            from INS_ANKETA
            where INS_ID = contract_id;

            v_division := GETUSERDIV(v_user_id);
        exception
            when no_data_found then
                raise_application_error(-20404, 'Contract doest not exist!');
                return -1;
        end;


        if ankprem > 0 and kurs > 0 then

            select count(1)
            into policy_count
            from INS_POLIS p
            where tb_anketa = contract_id
              and TB_STATUS = 2
              and TB_STATUS in (2, 9, 10);

            if policy_count = 0 then
                policy_series := 'EIND';
                SELECT INS_POLIS_BASIS.nextval * (-1) INTO policy_number FROM Dual;
                policy_id := ADD_POLIS(policy_series, policy_number, v_division, v_user_id);
                commit;

                begin
                    select INS_ID, OPL_DATA, OPL_TYPE, STATUS_PAYMENT, OPLATA
                    into payment_id, payment_date, v_payment_type, v_payment_status, v_oplata
                    from INS_OPLATA
                    where ANKETA_ID = contract_id
                      and OPL_TYPE <> 7
                      and rownum = 1;
                exception
                    when no_data_found then
                        raise_application_error(-20404, 'Payment does not exist!');
                end;

                select max(SHENGEN) into shn from INS_TRAVEL where ANKETA_ID = contract_id;

                if shn = 1 and days < 92 then
                    v_date_end := v_date_end + 15;
                else
                    v_date_end := v_date_end;
                end if;

                for rPSUGURTA IN (SELECT * FROM INS_PSUGURTA WHERE ANKETA_ID = contract_id)
                    LOOP
                        INSERT INTO INS_POLIS_SUGURTA
                        (ins_id,
                         ANKETA_ID,
                         PTURI_ID,
                         LINK_ID,
                         MAJBURIYAT,
                         MUKOFOT_F,
                         MUKOFOT,
                         OBEKT_SONI,
                         FRAN,
                         FRAN_CS,
                         FRAN_PR,
                         FRAN_SM,
                         DIVISION_ID,
                         DATA_BEGIN,
                         DATA_END,
                         DATA_VIDACHA,
                         POLIS_POOBYEKTAM,
                         POLIS_POOPLATE,
                         POLIS_SERY,
                         POLIS_NUMBER,
                         POLIS_ID,
                         VAL_TYPE,
                         VAL_DATE,
                         VAL_KURS)
                        VALUES (INS_POLIS_SUGURTA_seq.nextval,
                                rPSUGURTA.ANKETA_ID,
                                rPSUGURTA.PTURI_ID,
                                rPSUGURTA.LINK_ID,
                                rPSUGURTA.MAJBURIYAT,
                                rPSUGURTA.MUKOFOT_F,
                                rPSUGURTA.MUKOFOT,
                                rPSUGURTA.OBEKT_SONI,
                                rPSUGURTA.FRAN,
                                rPSUGURTA.FRAN_CS,
                                rPSUGURTA.FRAN_PR,
                                rPSUGURTA.FRAN_SM,
                                rPSUGURTA.DIVISION_ID,
                                v_date_begin,
                                v_date_end,
                                payment_date,
                                0,
                                0,
                                policy_series,
                                policy_number,
                                policy_id,
                                v_val_type,
                                payment_date,
                                v_val_kurs);
                    END LOOP;

                select sum(MAJBURIYAT),
                       sum(MUKOFOT),
                       sum(FRAN_SM)
                INTO psumma,pprem,pfran
                from INS_POLIS_SUGURTA
                where POLIS_ID = policy_id;

                UPDATE INS_POLIS
                SET TB_DATE_BEGIN   = v_date_begin,
                    TB_DATE_END     = v_date_end,
                    TB_RASTORG_DATE = v_date_end,
                    TB_ANKETA       = contract_id,
                    TB_SUMMA        = psumma,
                    TB_PREMIA       = pprem,
                    TB_FRANSHIZA    = pfran,
                    TB_DATEPRINT    = sysdate,
                    TB_DATECONTROL  = payment_date,
                    TB_USER         = v_user_id,
                    TB_AVTO         = null,
                    VAL_TYPE        = v_val_type,
                    VAL_DATE        = payment_date,
                    VAL_KURS        = v_val_kurs
                WHERE TB_ID = policy_id;

                UPDATE INS_ANKETA
                SET INS_STAT=2,
                    INS_PROGRESS = null
                WHERE INS_ID = contract_id;

                UPDATE INS_OPLATA
                SET POLIS_ID   = policy_id,
                    POLIS_SERY = 'EIND'
                WHERE INS_ID = payment_id;

                set_program_name_to_polis(contract_id);
/*
                select s.sp_online
                into v_is_online_pym
                from sp_typepl s
                where s.sp_id = v_payment_type;

                if v_is_online_pym = 1 and v_payment_status = 2 then
                    update INS_ANKETA
                    set IS_CHECK   = 1,
                        CHECK_USER = 1,
                        CHECK_DATE = sysdate
                    where INS_ID = contract_id;
                    commit;

                    for_1c_api.Payment_by_Online_Bc(v_oplata_id => payment_id,
                                                    v_oplata => v_oplata,
                                                    v_opl_date => payment_date,
                                                    v_div_id => v_division);
                    for_1c_api.Filling_BC_1C(v_opl_id => payment_id);

                    begin
                        update INS_BANK_CLIENT
                        set STATUS = 2
                        where DOC_NUM = payment_id;
                    end;
                end if;
*/
                if (HAS_ROLE(v_user_id, 'INSURER_ON_WEBSITE') > 0) then
                    UPDATE_POLICY_WITH_UUID(contract_id, policy_id, error, error_text);
                    commit;
                    v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONFIRM_PAYED(P_USER_ID => v_user_id,
                                                                          P_CONTRACT_ID => contract_id,
                                                                          P_POLICY_ID => policy_id,
                                                                          P_REQ_ID => 0,
                                                                          P_ERROR_CODE => error,
                                                                          P_ERROR_MESSAGE => error_text);
                    if error != 0 then
                        ROLLBACK;

                        UPDATE INS_POLIS
                        SET TB_STATUS = 1
                        WHERE TB_ID = policy_id;

                        return error;
                    end if;
                else

                    if v_user_id = 331 then
                        v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONTRACT_SANDBOX(P_USER_ID => v_user_id,
                                                                                 P_CONTRACT_ID => contract_id,
                                                                                 P_POLICY_ID => policy_id,
                                                                                 REQ_ID => -28,
                                                                                 P_ERROR_CODE => error,
                                                                                 P_ERROR_MESSAGE => error_text);
                    else
                        v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONTRACT(P_USER_ID => v_user_id,
                                                                         P_CONTRACT_ID => contract_id,
                                                                         P_POLICY_ID => policy_id,
                                                                         REQ_ID => 0,
                                                                         P_ERROR_CODE => error,
                                                                         P_ERROR_MESSAGE => error_text);
                    end if;
                    if error != 0 then
                        ROLLBACK;
                        update INS_POLIS SET TB_STATUS=1 WHERE TB_ID = policy_id;
                        return error;
                    end if;
                    if v_user_id = 331 then
                        v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONFIRM_PAYED_SANDBOX(P_USER_ID => v_user_id,
                                                                                      P_CONTRACT_ID => contract_id,
                                                                                      P_POLICY_ID => policy_id,
                                                                                      P_REQ_ID => -28,
                                                                                      P_ERROR_CODE => error,
                                                                                      P_ERROR_MESSAGE => error_text);
                    else
                        v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONFIRM_PAYED(P_USER_ID => v_user_id,
                                                                              P_CONTRACT_ID => contract_id,
                                                                              P_POLICY_ID => policy_id,
                                                                              P_REQ_ID => 0,
                                                                              P_ERROR_CODE => error,
                                                                              P_ERROR_MESSAGE => error_text);
                    end if;
                    if error != 0 then
                        ROLLBACK;
                        update INS_POLIS SET TB_STATUS=1 WHERE TB_ID = policy_id;
                        return error;
                    end if;
                end if;

                UPDATE INS_POLIS
                SET TB_STATUS = 2
                where TB_ID = policy_id;

                --- *** --- updated: 16.02.2026 Sardor Urokov
                select s.sp_online
                into v_is_online_pym
                from sp_typepl s
                where s.sp_id = v_payment_type;

                if v_is_online_pym = 1 then
                    update INS_ANKETA
                    set IS_CHECK   = 1,
                        CHECK_USER = 1,
                        INS_STAT   = 2,
                        CHECK_DATE = sysdate
                    where INS_ID = contract_id;
                    commit;

                    for_1c_api.Payment_by_Online_Bc(v_oplata_id => payment_id,
                                                    v_oplata => v_oplata,
                                                    v_opl_date => payment_date,
                                                    v_div_id => v_division);
                    for_1c_api.Filling_BC_1C(v_opl_id => payment_id);

                    begin
                        update INS_BANK_CLIENT
                        set STATUS = 2
                        where DOC_NUM = payment_id;
                    end;
                end if;

                --                 update INS_ANKETA
--                 set INS_STAT = 2
--                 where INS_ID = contract_id;
                --- *** ---

                select TB_SERY, TB_NUMBER, FOND_UID
                into policy_series,policy_number,policy_uuid
                from INS_POLIS
                where TB_ID = policy_id;

                update ins_oplata
                set POLIS_ID     = policy_id,
                    POLIS_SERY   = policy_series,
                    POLIS_NUMBER = policy_number,
                    opl_data     = sysdate
                where ANKETA_ID = contract_id;

                commit;

                /******************************** ОТПРАВКА СМС ********************************/
                select REGEXP_REPLACE(COALESCE(k.TB_PHONE1, k.TB_PHONE2, k.TB_PHONE3), '[^0-9]', '')
                into phone_number
                from ins_kontragent k
                         inner join INS_ANKETA a on a.owner = k.tb_id
                where a.ins_id = contract_id;

                V_SMS_TEXT :=
                        'Polis faol. Amal qilish muddati: ' || to_char(v_date_end, 'DD.MM.YYYY') || ' ' ||
                        CHR(10) ||
                        'Sayohatingizdan rohatlaning!' || CHR(10) ||
                        'Ваш полис активен. Срок действия: ' || to_char(v_date_end, 'DD.MM.YYYY') || ' ' ||
                        CHR(10) ||
                        'Приятного путешествия!' || CHR(10) ||
                        'https://api-travel.insonline.uz/api/travel/policy-old/' || policy_id || '/' || contract_id;
            else
                select tb_sery, tb_number, tb_id, FOND_UID
                into policy_series, policy_number, policy_id, policy_uuid
                from ins_polis
                where TB_ANKETA = contract_id;
                return policy_id;
            end if;
        end if;
        return policy_id;

    exception
        when others then
            rollback;
            error := sqlcode;
            error_text := DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
            return -1;
    end;

    function INSERT_TRAVELER(
        v_pinfl varchar2,
        v_pass_sery varchar2,
        v_pass_num varchar2,
        v_surname varchar2,
        v_given_name varchar2,
        v_patronymic varchar2,
        v_birth_day varchar2,
        v_pass_given varchar2,
        v_pass_given_date varchar2,
        v_resident number,
        v_citizenship in number,
        v_gender in number,
        v_phone in varchar2,
        v_region number,
        v_district number,
        v_address varchar2,
        v_user_id number,
        error_code out number,
        error_text out varchar2
    ) RETURN NUMBER IS
        v_kont_id number;
    begin
        error_code := 0;
        error_text := 'Successfully!';
        BEGIN
            select nvl(k.tb_id, 0)
            into v_kont_id
            from ins_kontragent k
            where k.tb_surname = v_surname
              and k.tb_name = v_given_name
              and k.tb_patronym = v_patronymic
              and k.tb_inps = v_pinfl
              and k.tb_paspsery = v_pass_sery
              and k.tb_paspnumber = v_pass_num
              and (v_address is not null and k.TB_ULICA = v_address)
              and ROWNUM = 1;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_kont_id := 0;
        END;

        if v_kont_id = 0 then
            select "INS_KONTRAGENT_SEQ".nextval into v_kont_id from Dual;
            insert into ins_kontragent(tb_id,
                                       tb_masterid,
                                       tb_surname,
                                       tb_name,
                                       tb_patronym,
                                       tb_fizyur,
                                       tb_datebirth,
                                       tb_paspsery,
                                       tb_paspnumber,
                                       tb_paspvidan,
                                       tb_paspdate,
                                       tb_rezident,
                                       TB_SEX,
                                       TB_PHONE1,
                                       TB_EMAIL,
                                       tb_country,
                                       tb_oblast,
                                       tb_rayon,
                                       tb_ulica,
                                       user_id,
                                       mod_user,
                                       tb_inps)
            values (v_kont_id,
                    v_kont_id,
                    v_surname,
                    v_given_name,
                    v_patronymic,
                    0,
                    to_date(SUBSTR(v_birth_day, 1, 10), 'yyyy-mm-dd'),
                    v_pass_sery,
                    v_pass_num,
                    v_pass_given,
                    CASE WHEN v_pass_given_date IS NULL THEN NULL WHEN v_pass_given_date LIKE '%.%.%' THEN TO_DATE(SUBSTR(v_pass_given_date, 1, 10), 'DD.MM.YYYY') WHEN v_pass_given_date LIKE '%-%-%' THEN TO_DATE(SUBSTR(v_pass_given_date, 1, 10), 'YYYY-MM-DD') ELSE NULL END,
                    v_resident,
                    v_gender,
                    v_phone,
                    v_phone,
                    v_citizenship,
                    v_region,
                    v_district,
                    v_address,
                    v_user_id,
                    v_user_id,
                    v_pinfl);
            COMMIT;
        end if;
        RETURN (v_kont_id);
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            error_code := -6;
            error_text := DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE() ||
                          v_birth_day || v_pass_given_date;
            RETURN -1;
    end;

    function CREATE_CONTRACT_FIZ(
        startDate in date,
        days in number,
        travel_type in number,
        multiId in number,
        programId in number,
        activityId in number,
        groupId in number,
        countries in varchar2,
        app_Fiz_yur in number,
        app_Date_Birth in date,
        app_Pass_Series in varchar2,
        app_Pass_Num in varchar2,
        app_Phone in varchar2,
        userId in number,
        people in INSURED_PERSON_TRAVEL,
        transaction_id in varchar2 default null,
        commission out number,
        policy_uuid out varchar2,
        out_error out number,
        out_error_text out varchar2
    ) RETURN number is
        v_contract_id     number;
        v_date_reg        date   := trunc(sysdate);
        v_ins_div         number;
        v_val_kurs        number;
        v_kurs_otv        number;
        v_owner           number;
        v_prem_usd        number;
        v_prem_uzs        number;
        v_end_date        date;
        v_liability       number;
        v_days            number;
        insured_people    INSURED_PERSON_TRAVEL;
        local_countries   varchar2(200);
        travel_id         number;
        cnt               number := 0;
        sumOTV            number;
        sumPREM           number;
        sumOTV_SUM        number;
        v_shengen         number;
        commision         number;
        sogl              number;
        v_client_id       number;
        v_doc_num         number;
        v_app_first_name  varchar2(4000);
        v_app_last_name   varchar2(4000);
        v_app_middle_name varchar2(4000);
        v_app_address     varchar2(4000);
        v_result_fond     varchar2(4000);
        rPSUGURTA         INS_PSUGURTA%ROWTYPE;
        v_val_type        number;
        psumma            number;
        pprem             number;
        pfran             number;
        payment_id        number;
        payment_date      date;
        policy_id         number;
        shn               number := 0;
        v_payment_type    number;
        v_payment_status  number;
        v_oplata          number;
        policy_series     varchar2(50);
        policy_number     number;
        v_ins_rate        number;
    BEGIN
        out_error := 0;
        out_error_text := 'Successfully completed!';

        insured_people := people;
        if travel_type = 0 then
            v_end_date := startDate + days - 1;
        else
            begin
                select ins_days
                into v_days
                from INS_ABROAD_MULTI
                where multiId = INS_ID;
            exception
                when no_data_found then
                    raise_application_error(-20422, 'Multi days provided is wrong!');
            end;
            v_end_date := startDate + v_days;
        end if;

        SELECT Ins_Anketa_Seq.NEXTVAL INTO v_contract_id FROM Dual;
        v_ins_div := getuserdiv(userId);

        v_kurs_otv := F_INS_GETKURS(3, v_date_reg);
        for c in (select *
                  from table (GET_PASSPORT_BIRTH_DATE(BIRTH_DATE => app_Date_Birth, PASS_SERY => app_Pass_Series,
                                                      PASS_NUM => app_Pass_Num)))
            loop
                if c.ERROR != 0 then
                    out_error := -2;
                    out_error_text :=
                            'No identity information was found through Passport data. (Birth date:' ||
                            TO_CHAR(app_Date_Birth, 'dd.mm.yyyy') || ' Passport serial:' || app_Pass_Series ||
                            ' Passport number:' || app_Pass_Num || ')';
                    return out_error;
                end if;
                v_app_first_name := c.FIRST_NAME_ENG;
                v_app_last_name := c.LAST_NAME_ENG;
                v_app_middle_name := c.MIDDLE_NAME;
                v_app_address := c.ADDRESS;
                v_owner := FOR_BANK_API.Insert_Kontragent(V_PINFL => c.PINFL,
                                                          V_PASS_SERY => app_Pass_Series,
                                                          V_PASS_NUM => app_Pass_Num,
                                                          V_SURNAME => nvl(c.LAST_NAME_ENG, c.LAST_NAME),
                                                          V_GIVEN_NAME => nvl(c.FIRST_NAME_ENG, c.FIRST_NAME),
                                                          V_PATRONYMIC => c.MIDDLE_NAME,
                                                          V_BIRTH_DAY => app_Date_Birth,
                                                          V_PASS_GIVEN => c.ISSUED_BY,
                                                          V_PASS_GIVEN_DATE => c.START_DATE,
                                                          V_REGION => c.REGION_ID,
                                                          V_DISTRICT => c.DISTRICT_ID,
                                                          V_ADDRESS => c.ADDRESS,
                                                          V_PHONE => app_Phone,
                                                          V_USER_ID => userId,
                                                          V_FIZ_YUR => 0,
                                                          V_INN => '',
                                                          V_ORGNAME => '',
                                                          V_ORGMFO => null);
                if v_owner = -1 then
                    out_error := -3;
                    out_error_text :=
                            DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
                    raise_application_error(-20404, 'Insurant did not saved!');
                    return out_error;
                end if;
            end loop;

        v_prem_usd :=
                advanced_calculator_fiz(travel_type => travel_type,
                                        activity_type => activityId,
                                        group_type => groupId,
                                        program_type => programId,
                                        days => days,
                                        multi_type => multiId,
                                        liability => v_liability,
                                        people => insured_people,
                                        curs => v_val_kurs,
                                        premium_uzs => v_prem_uzs,
                                        out_error_code => out_error,
                                        out_error_msg => out_error_text);

        if v_prem_usd < 0 then
            out_error := -4;
            out_error_text := 'The premium was considered negative. Enter the correct information for the calculator!';
            return out_error;
        end if;

        -- map fond returned countries to the local countries
        begin
            SELECT LISTAGG(SP_ID, ':') WITHIN GROUP (ORDER BY SP_ID)
            into local_countries
            from sp_country
            where sp_FOND_ID in (select COLUMN_VALUE from table (split(countries, ':')));
        exception
            when no_data_found then
                raise_application_error(-20422, 'Provided id of the country(ies) does not exist!');
        end;

        select max(SHENGEN)
        into v_shengen
        from sp_country
        where sp_FOND_ID in (select COLUMN_VALUE from table (split(countries, ':')));
        SELECT Sq_Tb_Polis.Nextval INTO v_doc_num FROM Dual;

        INSERT INTO ins_anketa
        (INS_ID,
         INS_DIV,
         INS_TYPE,
         OWNER,
         VAL_TYPE,
         VAL_KURS,
         VAL_USLOVIYA,
         POLIS_POOBYEKTAM,
         POLIS_POOPLATE,
         USLOVIE_OPLATI,
         USER_ID,
         INS_DATE_OSGOR,
         KURS_OTV,
         ABROAD_MIN,
         ABROAD_MAX,
         INS_DATE,
         ABROAD_POLIS_TYPE,
         INS_DATEF,
         INS_DATET,
         INS_DAY,
         ABROAD_DAYS,
         ABROAD_COUNTRY,
         ABROAD_PROGRAM,
         ABROAD_ACTIVITY,
         ABROAD_GROUP,
         ABROAD_PREM, OWN_NAME, own_address, own_phone, BENEFICIARY, FIZYUR, INS_DOGNUM)
        VALUES (v_contract_id,
                v_ins_div,
                17,
                v_owner,
                2,
                v_val_kurs,
                3,
                0,
                0,
                0,
                userId,
                v_date_reg,
                v_kurs_otv,
                v_prem_usd,
                v_prem_usd,
                v_date_reg,
                travel_type,
                startDate,
                v_end_date,
                days,
                multiId,
                local_countries,
                programId,
                activityId,
                groupId,
                v_prem_usd, v_app_last_name || ' ' || v_app_first_name || ' ' || v_app_middle_name, v_app_address,
                app_Phone,
                v_owner, 0, v_doc_num);
        COMMIT;

        select round(case
                         when A."INS_OTV" = 0 or A.INS_DAY = 0 then
                             null
                         else
                             A."INS_PREM" / A."INS_OTV" * 36500 /
                             (A.INS_DAY)
                         end,
                     4)
        into v_ins_rate
        from INS_ANKETA A
        where INS_ID = v_contract_id;

        FOR i in 1..insured_people.COUNT
            LOOP
                for pasd in (select *
                             from table (GET_PASSPORT_BIRTH_DATE(
                                                 BIRTH_DATE => TO_DATE(SUBSTR(insured_people(i).DATE_BIRTH, 1, 10), 'YYYY-MM-DD'),
                                                 PASS_SERY => insured_people(i).PASS_SERIES,
                                                 PASS_NUM => insured_people(i).PASS_NUMBER)))
                    loop
                        if pasd.ERROR != 0 then
                            out_error := -5;
                            out_error_text :=
                                    'No identity information was found through Passport data. (Birth date:' ||
                                    insured_people(i).DATE_BIRTH || ' Passport serial:' ||
                                    insured_people(i).PASS_SERIES ||
                                    ' Passport number:' || insured_people(i).PASS_NUMBER || ')';
                            return out_error;
                        end if;

                        v_client_id := FOR_TRAVEL_API_UPDATED.INSERT_TRAVELER(V_PINFL => pasd.PINFL,
                                                                              V_PASS_SERY => insured_people(i).PASS_SERIES,
                                                                              V_PASS_NUM => insured_people(i).PASS_NUMBER,
                                                                              V_SURNAME => nvl(pasd.LAST_NAME_ENG, pasd.LAST_NAME),
                                                                              V_GIVEN_NAME => nvl(pasd.FIRST_NAME_ENG, pasd.FIRST_NAME),
                                                                              V_PATRONYMIC => pasd.MIDDLE_NAME,
                                                                              V_BIRTH_DAY => insured_people(i).DATE_BIRTH,
                                                                              V_PASS_GIVEN => pasd.ISSUED_BY,
                                                                              V_PASS_GIVEN_DATE => to_char(pasd.END_DATE, 'yyy-mm-dd'),
                                                                              v_resident => 1,
                                                                              v_citizenship => 182,
                                                                              v_phone => app_Phone,
                                                                              v_gender => pasd.GENDER,
                                                                              V_REGION => pasd.REGION_ID,
                                                                              V_DISTRICT => pasd.DISTRICT_ID,
                                                                              V_ADDRESS => pasd.ADDRESS,
                                                                              V_USER_ID => userId,
                                                                              error_text => out_error_text,
                                                                              error_code => out_error);
                        if out_error != 0 then
                            ROLLBACK;
                            return out_error;
                        end if;
                        select INS_TRAVEL_SEQ.NEXTVAL into travel_id from dual;
                        INSERT INTO INS_TRAVEL
                        (INS_ID,
                         ANKETA_ID,
                         OTV,
                         PREM,
                         PREM_MIN,
                         PREM_MAX,
                         YEARS,
                         KOEF,
                         SHENGEN,
                         CLIENT_ID, DATEBIRTH)
                        VALUES (travel_id,
                                v_contract_id,
                                insured_people(i).LIABILITY,
                                insured_people(i).PREM,
                                insured_people(i).PREM_MIN,
                                insured_people(i).PREM_MAX,
                                insured_people(i).AGE,
                                insured_people(i).RATE,
                                v_shengen,
                                v_client_id, SUBSTR(insured_people(i).DATE_BIRTH, 1, 10));

                        --************************************INS_PSUGURTA_PO_OBYEKTAM******************************************

                        select count(1)
                        into cnt
                        from INS_PSUGURTA_PO_OBYEKTAM
                        where ANKETA_ID = v_contract_id
                          AND AVTO_ID = travel_id;

                        if cnt = 0 then
                            INSERT INTO INS_PSUGURTA_PO_OBYEKTAM (INS_ID,
                                                                  ANKETA_ID,
                                                                  AVTO_ID,
                                                                  LINK_ID,
                                                                  PTURI_ID,
                                                                  DIVISION_ID,
                                                                  VAL_TYPE,
                                                                  VAL_DATE,
                                                                  VAL_KURS,
                                                                  MUKOFOT,
                                                                  MUKOFOT_F,
                                                                  MAJBURIYAT,
                                                                  OBEKT_SONI,
                                                                  MUKOFOT2)
                            VALUES (INS_PSUGURTA_PO_OBYEKTAM_SEQ.nextval,
                                    v_contract_id,
                                    travel_id,
                                    3,
                                    17,
                                    v_ins_div,
                                    2,
                                    v_date_reg,
                                    v_val_kurs,
                                    insured_people(i).PREM,
                                    v_ins_rate,
                                    insured_people(i).LIABILITY,
                                    1,
                                    round(insured_people(i).PREM * v_val_kurs));
                            COMMIT;
                        else
                            update INS_PSUGURTA_PO_OBYEKTAM
                            set VAL_DATE   = v_date_reg,
                                VAL_KURS   = v_val_kurs,
                                MUKOFOT    = v_prem_usd,
                                MUKOFOT2   = v_prem_uzs,
                                MAJBURIYAT = v_liability
                            where ANKETA_ID = v_contract_id
                              and AVTO_ID = travel_id
                              and LINK_ID = 3;
                            COMMIT;
                        end if;


                        --************************************INS_PSUGURTA******************************************
                        FOR rw IN (select po.LINK_ID                      as
                                              LINK_ID,
                                          sum(po.MAJBURIYAT)              as
                                              MAJBURIYAT,
                                          sum(po.MUKOFOT)                 as
                                              MUKOFOT,
                                          sum(po.OBEKT_SONI)              as
                                              OBEKT_SONI,
                                          sum(po.FRAN_SM)                 as
                                              FRAN_SM,
                                          sum(po.MAJBURIYAT * v_val_kurs) as
                                              MAJBURIYAT_SUM
                                   from INS_PSUGURTA_PO_OBYEKTAM po
                                   where po.ANKETA_ID = v_contract_id
                                   group by po.LINK_ID
                                   order by po.LINK_ID)
                            LOOP
                                select count(1)
                                INTO cnt
                                from INS_PSUGURTA
                                where ANKETA_ID = v_contract_id
                                  and LINK_ID = rw.LINK_ID;

                                IF
                                    cnt > 0 THEN
                                    UPDATE INS_PSUGURTA
                                    SET MAJBURIYAT=rw.MAJBURIYAT,
                                        MUKOFOT=rw.MUKOFOT,
                                        OBEKT_SONI=rw.OBEKT_SONI,
                                        FRAN_SM=rw.FRAN_SM,
                                        VAL_TYPE = 2,
                                        VAL_DATE = v_date_reg,
                                        VAL_KURS = v_val_kurs,
                                        MAJBURIYAT_SUM = rw.MAJBURIYAT_SUM
                                    where ANKETA_ID = v_contract_id
                                      and LINK_ID = rw.LINK_ID;
                                    COMMIT;
                                ELSE
                                    INSERT INTO INS_PSUGURTA (INS_ID,
                                                              ANKETA_ID,
                                                              PTURI_ID,
                                                              LINK_ID,
                                                              MAJBURIYAT,
                                                              MUKOFOT,
                                                              OBEKT_SONI,
                                                              FRAN_SM,
                                                              DIVISION_ID,
                                                              VAL_TYPE,
                                                              VAL_DATE,
                                                              VAL_KURS,
                                                              MAJBURIYAT_SUM)
                                    VALUES (INS_PSUGURTA_SEQ.nextval,
                                            v_contract_id,
                                            17,
                                            rw.LINK_ID,
                                            rw.MAJBURIYAT,
                                            rw.MUKOFOT,
                                            rw.OBEKT_SONI,
                                            rw.FRAN_SM,
                                            v_ins_div,
                                            2,
                                            v_date_reg,
                                            v_val_kurs,
                                            rw.MAJBURIYAT_SUM);
                                    COMMIT;
                                END IF;
                            END LOOP;
                    end loop;

            END LOOP;
        COMMIT;

        -- get insurance liability based on the program id


        --************************************INS_ANKETA******************************************
        SELECT sum(MAJBURIYAT), sum(MUKOFOT), sum(MAJBURIYAT_SUM)
        INTO sumOTV, sumPREM, sumOTV_SUM
        FROM INS_PSUGURTA
        WHERE ANKETA_ID = v_contract_id;


        UPDATE INS_ANKETA
        SET INS_OTV      = sumOTV,
            INS_OTV_SUM  = sumOTV_SUM,
            INS_PREM     = sumPREM,
            INS_PROGRESS = null
        WHERE INS_ID = v_contract_id;

        select round(case
                         when A."INS_OTV" = 0 or A.INS_DAY = 0 then
                             null
                         else
                             A."INS_PREM" / A."INS_OTV" * 36500 /
                             (A.INS_DAY)
                         end,
                     4)
        into v_ins_rate
        from INS_ANKETA A
        where INS_ID = v_contract_id;
        update INS_PSUGURTA_PO_OBYEKTAM set MUKOFOT_F=v_ins_rate where ANKETA_ID = v_contract_id;
        COMMIT;

        -- ******************* INS_OPLATA *********************
        if v_prem_usd > 0 and v_val_kurs > 0 then
            select count(*) into cnt from INS_OPLATA where ANKETA_ID = v_contract_id;

            commision := F_INS_GETKOMMIS(userId, 17, v_date_reg);
            sogl := F_INS_GETSOGL(userId, v_date_reg);


            if cnt = 0 then
                insert into INS_OPLATA (INS_ID,
                                        ANKETA_ID,
                                        DIVISION_ID,
                                        USER_ID,
                                        OPL_DATA,
                                        OPL_SUMMA,
                                        INS_TYPE,
                                        OPL_TYPE,
                                        VAL_TYPE,
                                        STATUS,
                                        VAL_KURS,
                                        OPLATA,
                                        OPL_VAL,
                                        KOMMIS_F,
                                        KOMMIS_SUMMA,
                                        SOGL_ID,
                                        AGENCY_TRANSACTION_ID)
                values (INS_OPLATA_SEQ.nextval,
                        v_contract_id,
                        v_ins_div,
                        userId,
                        v_date_reg,
                        sumPREM,
                        17,
                        3,
                        2,
                        0,
                        v_val_kurs,
                        v_prem_uzs,
                        1,
                        commision,
                        round(v_prem_uzs * commision / 100),
                        sogl,
                        transaction_id);
                COMMIT;
            else
                update ins_oplata
                set OPL_DATA  = v_date_reg,
                    OPL_SUMMA = sumPREM,
                    VAL_KURS  = v_val_kurs,
                    OPLATA    = round(sumPREM * v_val_kurs),
                    OPL_TYPE  = 3
                where ANKETA_ID = v_contract_id;
                COMMIT;
            end if;

        end if; --ankprem>0

        if (HAS_ROLE(userId, 'INSURER_ON_WEBSITE') > 0) then
            --************************************INS_POLICY******************************************
--             policy_series := 'EIND';
--             SELECT INS_POLIS_BASIS.nextval * (-1) INTO policy_number FROM Dual;
--             policy_id := ADD_POLIS(policy_series, policy_number, v_ins_div, userId);
-- 
--             begin
--                 select INS_ID, OPL_DATA, OPL_TYPE, STATUS_PAYMENT, OPLATA
--                 into payment_id, payment_date, v_payment_type, v_payment_status, v_oplata
--                 from INS_OPLATA
--                 where ANKETA_ID = v_contract_id
--                   and OPL_TYPE <> 7
--                   and rownum = 1;
--             exception
--                 when no_data_found then
--                     raise_application_error(-20404, 'Payment does not exist!');
--             end;
-- 
--             select max(SHENGEN) into shn from INS_TRAVEL where ANKETA_ID = v_contract_id;
-- 
--             if shn = 1 and days < 92 then
--                 v_end_date := v_end_date + 15;
--             else
--                 v_end_date := v_end_date;
--             end if;
-- 
--             for rPSUGURTA IN (SELECT * FROM INS_PSUGURTA WHERE ANKETA_ID = v_contract_id)
--                 LOOP
--                     INSERT INTO INS_POLIS_SUGURTA
--                     (ins_id,
--                      ANKETA_ID,
--                      PTURI_ID,
--                      LINK_ID,
--                      MAJBURIYAT,
--                      MUKOFOT_F,
--                      MUKOFOT,
--                      OBEKT_SONI,
--                      FRAN,
--                      FRAN_CS,
--                      FRAN_PR,
--                      FRAN_SM,
--                      DIVISION_ID,
--                      DATA_BEGIN,
--                      DATA_END,
--                      DATA_VIDACHA,
--                      POLIS_POOBYEKTAM,
--                      POLIS_POOPLATE,
--                      POLIS_SERY,
--                      POLIS_NUMBER,
--                      POLIS_ID,
--                      VAL_TYPE,
--                      VAL_DATE,
--                      VAL_KURS)
--                     VALUES (INS_POLIS_SUGURTA_seq.nextval,
--                             rPSUGURTA.ANKETA_ID,
--                             rPSUGURTA.PTURI_ID,
--                             rPSUGURTA.LINK_ID,
--                             rPSUGURTA.MAJBURIYAT,
--                             rPSUGURTA.MUKOFOT_F,
--                             rPSUGURTA.MUKOFOT,
--                             rPSUGURTA.OBEKT_SONI,
--                             rPSUGURTA.FRAN,
--                             rPSUGURTA.FRAN_CS,
--                             rPSUGURTA.FRAN_PR,
--                             rPSUGURTA.FRAN_SM,
--                             rPSUGURTA.DIVISION_ID,
--                             startDate,
--                             v_end_date,
--                             payment_date,
--                             0,
--                             0,
--                             policy_series,
--                             policy_number,
--                             policy_id,
--                             v_val_type,
--                             payment_date,
--                             v_val_kurs);
--                 END LOOP;
-- 
--             select sum(MAJBURIYAT),
--                    sum(MUKOFOT),
--                    sum(FRAN_SM)
--             INTO psumma,pprem,pfran
--             from INS_POLIS_SUGURTA
--             where POLIS_ID = policy_id;
-- 
--             UPDATE INS_POLIS
--             SET TB_DATE_BEGIN   = startDate,
--                 TB_DATE_END     = v_end_date,
--                 TB_RASTORG_DATE = v_end_date,
--                 TB_ANKETA       = v_contract_id,
--                 TB_SUMMA        = psumma,
--                 TB_PREMIA       = pprem,
--                 TB_FRANSHIZA    = pfran,
--                 TB_DATEPRINT    = sysdate,
--                 TB_DATECONTROL  = payment_date,
--                 TB_USER         = userId,
--                 TB_AVTO         = null,
--                 VAL_TYPE        = v_val_type,
--                 VAL_DATE        = payment_date,
--                 VAL_KURS        = v_val_kurs
--             WHERE TB_ID = policy_id;
-- 
--             v_result_fond := ERSP_VOLUNTARY_INTEGRATIONS.CONTRACT(userId,
--                                                                   v_contract_id,
--                                                                   policy_id,
--                                                                   0,
--                                                                   out_error,
--                                                                   out_error_text);
--             if out_error != 0 then
--                 ROLLBACK;
--                 return out_error;
--             end if;
-- 
--             return v_contract_id;

            v_result_fond := ERSP_VOLUNTARY_INTEGRATIONS.CONTRACT(userId,
                                                                  v_contract_id,
                                                                  null,
                                                                  null,
                                                                  out_error,
                                                                  out_error_text);
            if out_error != 0 then
                ROLLBACK;
                return out_error;
            end if;
        end if;

        return v_contract_id;
    exception
        when others THEN
            OUT_ERROR := sqlcode;
            ASBT_SYS.INS_ERR('Travel error: ' || dbms_utility.format_error_backtrace ||
                             dbms_utility.format_error_stack);
            OUT_ERROR_TEXT := DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
            return -1;
    END CREATE_CONTRACT_FIZ;

    FUNCTION GET_POLICY_LINK(
        contract_id in number,
        p_user_id in number,
        policy_link out varchar2,
        error out number,
        error_text out varchar2
    ) RETURN NUMBER IS
        v_cnt       number;
        policy_uuid varchar2(4000);
    BEGIN
        error := 0;
        error_text := 'Successfully!';

        select count(*) into v_cnt from INS_ANKETA where INS_ID = contract_id and USER_ID = p_user_id;

        if v_cnt < 1 then
            error := -1;
            error_text := 'Contract is not found!';
            return error;
        end if;

        select count(*)
        into v_cnt
        from INS_POLIS
        where TB_ANKETA = contract_id
          and TB_STATUS = 2
          and FOND_UID is not null
          and TB_USER = p_user_id;
        if v_cnt < 1 then
            error := -2;
            error_text := 'Police information not found!';
            return error;
        end if;
        select FOND_UID
        into policy_uuid
        from INS_POLIS
        where TB_ANKETA = contract_id
          and TB_STATUS = 2
          and FOND_UID is not null
          and TB_USER = p_user_id;

        policy_link := 'https://ersp.e-osgo.uz/site/export-to-pdf?oldAlso=yes&id=' || policy_uuid;
        return 0;
    EXCEPTION
        when others THEN
            error := -999;
            error_text := DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
            return -1;
    END GET_POLICY_LINK;

    function CREATE_CONTRACT_FIZ_V2(
        startDate in date,
        days in number,
        travel_type in number,
        multiId in number,
        programId in number,
        activityId in number,
        groupId in number,
        countries in varchar2,
        app_resident in number,
        app_citizenship in number,
        app_firstname in varchar2,
        app_lastname in varchar2,
        app_middlename in varchar2,
        app_email in varchar2,
        app_gender in number,
        app_Date_Birth in date,
        app_Pass_Series in varchar2,
        app_Pass_Num in varchar2,
        app_Phone in varchar2,
        userId in number,
        people in INSURED_PERSON_TRAVEL,
        transaction_id in varchar2 default null,
        commission out number,
        policy_uuid out varchar2,
        out_error out number,
        out_error_text out varchar2
    ) RETURN number is
        v_contract_id            number;
        v_date_reg               date   := trunc(sysdate);
        v_ins_div                number;
        v_val_kurs               number;
        v_kurs_otv               number;
        v_owner                  number;
        v_prem_usd               number;
        v_prem_uzs               number;
        v_end_date               date;
        v_liability              number;
        v_days                   number;
        insured_people           INSURED_PERSON_TRAVEL;
        local_countries          varchar2(200);
        travel_id                number;
        cnt                      number := 0;
        sumOTV                   number;
        sumPREM                  number;
        sumOTV_SUM               number;
        v_shengen                number;
        commision                number;
        sogl                     number;
        rate                     number;
        v_client_id              number;
        v_doc_num                number;
        v_app_first_name         varchar2(4000);
        v_app_last_name          varchar2(4000);
        v_app_middle_name        varchar2(4000);
        v_app_address            varchar2(4000);
        v_result_fond            varchar2(4000);
        rPSUGURTA                INS_PSUGURTA%ROWTYPE;
        v_val_type               number;
        psumma                   number;
        pprem                    number;
        pfran                    number;
        payment_id               number;
        payment_date             date;
        policy_id                number;
        shn                      number := 0;
        v_payment_type           number;
        v_payment_status         number;
        v_oplata                 number;
        policy_series            varchar2(50);
        policy_number            number;
        v_sp_country_id          number;
        v_sp_country_traveler_id number;
        v_create_contract_first  number(1);
        v_resident_name          varchar2(400);
        v_premium_sum            number;
    BEGIN
        out_error := 0;
        out_error_text := 'Successfully completed!';

        insured_people := people;
        if travel_type = 0 then
            v_end_date := startDate + days - 1;
        else
            begin
                select ins_days
                into v_days
                from INS_ABROAD_MULTI
                where multiId = INS_ID;
            exception
                when no_data_found then
                    raise_application_error(-20422, 'Multi days provided is wrong!');
            end;
            v_end_date := startDate + v_days;
        end if;

        SELECT Ins_Anketa_Seq.NEXTVAL INTO v_contract_id FROM Dual;
        v_ins_div := getuserdiv(userId);

        select nvl(send_napp_first, 0)
        into v_create_contract_first
        from TB_USERS
        where tb_id = userId;

        v_kurs_otv := F_INS_GETKURS(3, v_date_reg);

        begin
            select SP_NAME2
            into v_resident_name
            from SP_RESIDENT
            where sp_id = app_resident
              and SP_ACTIVE = 1;
        exception
            when others then
                out_error := -5;
                out_error_text := 'Applicant resident type not found!';
                return out_error;
        end;

        if app_resident = 2 then
            begin
                -- select SP_ID into v_sp_country_id from SP_COUNTRY where SP_FOND_ID = app_citizenship;
                select SP_ID
                into v_sp_country_id
                from SP_COUNTRY
                where SP_FOND_ID = app_citizenship
                  and sp_active = 1
                  and ROWNUM = 1;  -- naumov
            exception
                when no_data_found then
                    out_error := -1;
                    out_error_text := 'Applicant country not found!';
                    return out_error;
            end;
        end if;

        -- resident
        if app_resident = 1 then
            for c in (select *
                      from table (GET_PASSPORT_BIRTH_DATE(BIRTH_DATE => app_Date_Birth, PASS_SERY => app_Pass_Series,
                                                          PASS_NUM => app_Pass_Num)))
                loop
                    if c.ERROR != 0 then
                        out_error := -2;
                        out_error_text :=
                                'No identity information was found through Passport data. (Birth date:' ||
                                TO_CHAR(app_Date_Birth, 'dd.mm.yyyy') || ' Passport serial:' || app_Pass_Series ||
                                ' Passport number:' || app_Pass_Num || ')';
                        return out_error;
                    end if;
                    v_app_first_name := c.FIRST_NAME_ENG;
                    v_app_last_name := c.LAST_NAME_ENG;
                    v_app_middle_name := c.MIDDLE_NAME;
                    v_app_address := c.ADDRESS;
                    v_owner := Insert_Kontragent_FIZ(V_PINFL => c.PINFL,
                                                     V_PASS_SERY => app_Pass_Series,
                                                     V_PASS_NUM => app_Pass_Num,
                                                     V_SURNAME => nvl(c.LAST_NAME_ENG, c.LAST_NAME),
                                                     V_GIVEN_NAME => nvl(c.FIRST_NAME_ENG, c.FIRST_NAME),
                                                     V_PATRONYMIC => c.MIDDLE_NAME,
                                                     V_BIRTH_DAY => app_Date_Birth,
                                                     V_PASS_GIVEN => c.ISSUED_BY,
                                                     V_PASS_GIVEN_DATE => c.START_DATE,
                                                     V_RESIDENT => app_resident,
                                                     V_EMAIL => nvl(app_email, null),
                                                     V_GENDER => app_gender,
                                                     V_COUNTRY_ID => 182,
                                                     V_REGION => c.REGION_ID,
                                                     V_DISTRICT => c.DISTRICT_ID,
                                                     V_ADDRESS => c.ADDRESS,
                                                     V_PHONE => app_Phone,
                                                     V_USER_ID => userId,
                                                     V_FIZ_YUR => 0,
                                                     V_INN => '',
                                                     V_ORGNAME => '',
                                                     V_ORGMFO => null);

                    if v_owner = -1 then
                        out_error := -3;
                        out_error_text :=
                                DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
                        raise_application_error(-20404, 'Insurant did not saved!');
                        return out_error;
                    end if;
                end loop;
        else
            -- non-resident
            v_app_first_name := app_firstname;
            v_app_last_name := app_lastname;
            v_app_middle_name := app_middlename;
            v_app_address := '';
            v_owner := Insert_Kontragent_FIZ(V_PINFL => '',
                                             V_PASS_SERY => app_Pass_Series,
                                             V_PASS_NUM => app_Pass_Num,
                                             V_SURNAME => v_app_first_name,
                                             V_GIVEN_NAME => v_app_last_name,
                                             V_PATRONYMIC => v_app_middle_name,
                                             V_BIRTH_DAY => app_Date_Birth,
                                             V_PASS_GIVEN => '',
                                             V_PASS_GIVEN_DATE => '',
                                             V_RESIDENT => app_resident,
                                             V_EMAIL => app_email,
                                             V_GENDER => app_gender,
                                             V_COUNTRY_ID => v_sp_country_id,
                                             V_REGION => null,
                                             V_DISTRICT => null,
                                             V_ADDRESS => '',
                                             V_PHONE => app_Phone,
                                             V_USER_ID => userId,
                                             V_FIZ_YUR => 0,
                                             V_INN => '',
                                             V_ORGNAME => '',
                                             V_ORGMFO => null);
            if v_owner = -1 then
                out_error := -3;
                out_error_text :=
                        DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
                raise_application_error(-20404, 'Insurant did not saved!');
                return out_error;
            end if;
        end if;

        v_prem_usd :=
                advanced_calculator_fiz(travel_type => travel_type,
                                        activity_type => activityId,
                                        group_type => groupId,
                                        program_type => programId,
                                        days => days,
                                        multi_type => multiId,
                                        liability => v_liability,
                                        people => insured_people,
                                        curs => v_val_kurs,
                                        premium_uzs => v_prem_uzs,
                                        out_error_code => out_error,
                                        out_error_msg => out_error_text);
        
        if v_prem_usd < 0 then
            ROLLBACK;
            out_error := -4;
            out_error_text := 'The premium was considered negative. Enter the correct information for the calculator!';
            return out_error;
        end if;

        -- map fond returned countries to the local countries
--         IF countries IS NOT NULL THEN
--             begin
--                 SELECT LISTAGG(SP_ID, ':') WITHIN GROUP (ORDER BY SP_ID)
--                 into local_countries
--                 from sp_country
--                 where SP_FOND_ID in (select COLUMN_VALUE from table (split(countries, ':')));
--             exception
--                 when no_data_found then
--                     ROLLBACK;
--                     raise_application_error(-20422, 'Provided id of the country(ies) does not exist!');
--             end;
--
--             select max(SHENGEN)
--             into v_shengen
--             from sp_country
--             where SP_FOND_ID in (select COLUMN_VALUE from table (split(countries, ':')));

--         ELSE
        begin
            SELECT LISTAGG(SP_ID, ':') WITHIN GROUP (ORDER BY SP_ID)
            into local_countries
            from sp_country
            where SP_KOD_ALPHA_2 in (select COLUMN_VALUE from table (split(countries, ':')));

            -- >>> БЛОК ДЛЯ ВАЛИДАЦИИ ПРОГРАММЫ <<<

        DECLARE
            v_not_found_list VARCHAR2(4000) := '';
            v_current_id     NUMBER;
            v_pr_allowed     NUMBER;
            v_resolved_ids   VARCHAR2(4000) := '';
            v_program_denied NUMBER := 0;
        BEGIN
            FOR r IN (SELECT COLUMN_VALUE as val FROM table(split(countries, ':'))) LOOP
                BEGIN
                    -- ИСПРАВЛЕНИЕ: Ищем по SP_FOND_ID, а не по SP_ID
                    SELECT SP_ID, 
                           CASE programId 
                                WHEN 1 THEN pr1 WHEN 2 THEN pr2 WHEN 3 THEN pr3 
                                WHEN 4 THEN pr4 WHEN 5 THEN pr5 WHEN 6 THEN pr6 
                                ELSE 0 
                           END
                    INTO v_current_id, v_pr_allowed
                    FROM sp_country
                    WHERE to_char(SP_FOND_ID) = r.val OR upper(SP_KOD_ALPHA_2) = upper(r.val)
                    AND rownum = 1
                    AND SP_ACTIVE = 1;

                    -- Собираем локальные (внутренние) ID стран
                    v_resolved_ids := v_resolved_ids || v_current_id || ':';
                    
                    -- Проверяем доступность программы
                    IF v_pr_allowed = 0 OR v_pr_allowed IS NULL THEN
                        v_program_denied := 1;
                    END IF;

                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        v_not_found_list := v_not_found_list || r.val || ', ';
                END;
            END LOOP;

            IF v_not_found_list IS NOT NULL THEN
                ROLLBACK;
                out_error := 400;
                out_error_text := 'Value(s) [' || rtrim(v_not_found_list, ', ') || '] not recognized as a valid country ID or code.';
                RETURN -1;
            END IF;

            IF v_program_denied = 1 THEN
                ROLLBACK;
                out_error := 400;
                out_error_text := 'Selected program is not allowed for one or more requested countries.';
                RETURN -1;
            END IF;

            -- Переприсваиваем переменную для использования дальше в процедуре
            local_countries := rtrim(v_resolved_ids, ':');
        END;
            -- >>> КОНЕЦ ВАЛИДАЦИИ <<<

        exception
            when no_data_found then
                ROLLBACK;
                raise_application_error(-20422, 'Provided id of the country(ies) does not exist!');
        end;

        select max(SHENGEN)
        into v_shengen
        from sp_country
        where SP_KOD_ALPHA_2 in (select COLUMN_VALUE from table (split(countries, ':')));
--         END IF;

        SELECT Sq_Tb_Polis.Nextval INTO v_doc_num FROM Dual;

        INSERT INTO ins_anketa
        (INS_ID,
         INS_DIV,
         INS_TYPE,
         OWNER,
         VAL_TYPE,
         VAL_KURS,
         VAL_USLOVIYA,
         POLIS_POOBYEKTAM,
         POLIS_POOPLATE,
         USLOVIE_OPLATI,
         USER_ID,
         INS_DATE_OSGOR,
         KURS_OTV,
         ABROAD_MIN,
         ABROAD_MAX,
         INS_DATE,
         ABROAD_POLIS_TYPE,
         INS_DATEF,
         INS_DATET,
         INS_DAY,
         ABROAD_DAYS,
         ABROAD_COUNTRY,
         ABROAD_PROGRAM,
         ABROAD_ACTIVITY,
         ABROAD_GROUP,
         ABROAD_PREM,
         OWN_NAME,
         own_address,
         own_phone,
         BENEFICIARY,
         FIZYUR,
         INS_DOGNUM)
        VALUES (v_contract_id,
                v_ins_div,
                17,
                v_owner,
                2,
                v_val_kurs,
                3,
                0,
                0,
                0,
                userId,
                v_date_reg,
                v_kurs_otv,
                v_prem_usd,
                v_prem_usd,
                v_date_reg,
                travel_type,
                startDate,
                v_end_date,
                days,
                multiId,
                local_countries,
                programId,
                activityId,
                groupId,
                v_prem_usd,
                v_app_last_name || ' ' || v_app_first_name || ' ' || v_app_middle_name,
                v_app_address,
                app_Phone,
                v_owner,
                0,
                v_doc_num);
        COMMIT;

        FOR i in 1..insured_people.COUNT
            LOOP
                -- non-resident
                if insured_people(i).IS_RESIDENT = 2 then
                    begin
                        select SP_ID
                        into v_sp_country_traveler_id
                        from SP_COUNTRY
                        where SP_FOND_ID = insured_people(i).CITIZENSHIP
                        and sp_active = 1
                        and ROWNUM = 1;
                    exception
                        when no_data_found then
                            out_error := -1;
                            out_error_text := 'Traveler citizenship country not found!';
                            return out_error;
                    end;

                    select case
                               when insured_people(i).FIRST_NAME is null then -2
                               when insured_people(i).LAST_NAME is null then -3
                               when insured_people(i).GENDER is null then -4
                               when insured_people(i).CITIZENSHIP is null then -5
                               else 0
                               end,
                           case
                               when insured_people(i).FIRST_NAME is null then 'Nonresident Traveler Firstname cannot be null'
                               when insured_people(i).LAST_NAME is null then 'Nonresident Traveler Lastname cannot be null'
                               when insured_people(i).GENDER is null then 'Nonresident Traveler Gender cannot be null'
                               when insured_people(i).CITIZENSHIP is null then 'Nonresident Traveler Citizenship cannot be null'
                               else 'successfully done'
                               end
                    into out_error, out_error_text
                    from dual;

                    if out_error <> 0 then
                        return out_error;
                    end if;

                    v_client_id := FOR_TRAVEL_API_UPDATED.INSERT_TRAVELER(V_PINFL => '',
                                                                          V_PASS_SERY => insured_people(i).PASS_SERIES,
                                                                          V_PASS_NUM => insured_people(i).PASS_NUMBER,
                                                                          V_SURNAME => insured_people(i).LAST_NAME,
                                                                          V_GIVEN_NAME => insured_people(i).FIRST_NAME,
                                                                          V_PATRONYMIC => insured_people(i).MIDDLE_NAME,
                                                                          V_BIRTH_DAY => insured_people(i).DATE_BIRTH,
                                                                          V_PASS_GIVEN => '',
                                                                          V_PASS_GIVEN_DATE => '',
                                                                          V_RESIDENT => insured_people(i).IS_RESIDENT,
                                                                          V_GENDER => insured_people(i).GENDER,
                                                                          V_PHONE => insured_people(i).PHONE,
                                                                          V_CITIZENSHIP => v_sp_country_traveler_id,
                                                                          V_REGION => null,
                                                                          V_DISTRICT => null,
                                                                          V_ADDRESS => '',
                                                                          V_USER_ID => userId,
                                                                          error_text => out_error_text,
                                                                          error_code => out_error);
                    if out_error != 0 then
                        ROLLBACK;
                        return out_error;
                    end if;

                    select INS_TRAVEL_SEQ.NEXTVAL into travel_id from dual;
                    INSERT INTO INS_TRAVEL
                    (INS_ID,
                     ANKETA_ID,
                     OTV,
                     PREM,
                     PREM_MIN,
                     PREM_MAX,
                     YEARS,
                     KOEF,
                     SHENGEN,
                     CLIENT_ID, DATEBIRTH)
                    VALUES (travel_id,
                            v_contract_id,
                            insured_people(i).LIABILITY,
                            insured_people(i).PREM,
                            insured_people(i).PREM_MIN,
                            insured_people(i).PREM_MAX,
                            insured_people(i).AGE,
                            insured_people(i).RATE,
                            v_shengen,
                            v_client_id, SUBSTR(insured_people(i).DATE_BIRTH, 1, 10));

                    --************************************INS_PSUGURTA_PO_OBYEKTAM******************************************
                    rate := insured_people(i).PREM / insured_people(i).LIABILITY * 100;
                    select count(1)
                    into cnt
                    from INS_PSUGURTA_PO_OBYEKTAM
                    where ANKETA_ID = v_contract_id
                      AND AVTO_ID = travel_id;

                    if cnt = 0 then
                        INSERT INTO INS_PSUGURTA_PO_OBYEKTAM (INS_ID,
                                                              ANKETA_ID,
                                                              AVTO_ID,
                                                              LINK_ID,
                                                              PTURI_ID,
                                                              DIVISION_ID,
                                                              VAL_TYPE,
                                                              VAL_DATE,
                                                              VAL_KURS,
                                                              MUKOFOT,
                                                              MUKOFOT_F,
                                                              MAJBURIYAT,
                                                              OBEKT_SONI,
                                                              MUKOFOT2)
                        VALUES (INS_PSUGURTA_PO_OBYEKTAM_SEQ.nextval,
                                v_contract_id,
                                travel_id,
                                3,
                                17,
                                v_ins_div,
                                2,
                                v_date_reg,
                                v_val_kurs,
                                insured_people(i).PREM,
                                rate,
                                insured_people(i).LIABILITY,
                                1,
                                round(insured_people(i).PREM * v_val_kurs));
                    else
                        update INS_PSUGURTA_PO_OBYEKTAM
                        set VAL_DATE   = v_date_reg,
                            VAL_KURS   = v_val_kurs,
                            MUKOFOT    = v_prem_usd,
                            MUKOFOT2   = v_prem_uzs,
                            MAJBURIYAT = v_liability
                        where ANKETA_ID = v_contract_id
                          and AVTO_ID = travel_id
                          and LINK_ID = 3;
                    end if;


                    --************************************INS_PSUGURTA******************************************
                    FOR rw IN (select po.LINK_ID                      as
                                          LINK_ID,
                                      sum(po.MAJBURIYAT)              as
                                          MAJBURIYAT,
                                      sum(po.MUKOFOT)                 as
                                          MUKOFOT,
                                      sum(po.OBEKT_SONI)              as
                                          OBEKT_SONI,
                                      sum(po.FRAN_SM)                 as
                                          FRAN_SM,
                                      sum(po.MAJBURIYAT * v_val_kurs) as
                                          MAJBURIYAT_SUM
                               from INS_PSUGURTA_PO_OBYEKTAM po
                               where po.ANKETA_ID = v_contract_id
                               group by po.LINK_ID
                               order by po.LINK_ID)
                        LOOP
                            select count(1)
                            INTO cnt
                            from INS_PSUGURTA
                            where ANKETA_ID = v_contract_id
                              and LINK_ID = rw.LINK_ID;

                            IF
                                cnt > 0 THEN
                                UPDATE INS_PSUGURTA
                                SET MAJBURIYAT=rw.MAJBURIYAT,
                                    MUKOFOT=rw.MUKOFOT,
                                    OBEKT_SONI=rw.OBEKT_SONI,
                                    FRAN_SM=rw.FRAN_SM,
                                    VAL_TYPE = 2,
                                    VAL_DATE = v_date_reg,
                                    VAL_KURS = v_val_kurs,
                                    MAJBURIYAT_SUM = rw.MAJBURIYAT_SUM
                                where ANKETA_ID = v_contract_id
                                  and LINK_ID = rw.LINK_ID;
                            ELSE
                                INSERT INTO INS_PSUGURTA (INS_ID,
                                                          ANKETA_ID,
                                                          PTURI_ID,
                                                          LINK_ID,
                                                          MAJBURIYAT,
                                                          MUKOFOT,
                                                          OBEKT_SONI,
                                                          FRAN_SM,
                                                          DIVISION_ID,
                                                          VAL_TYPE,
                                                          VAL_DATE,
                                                          VAL_KURS,
                                                          MAJBURIYAT_SUM)
                                VALUES (INS_PSUGURTA_SEQ.nextval,
                                        v_contract_id,
                                        17,
                                        rw.LINK_ID,
                                        rw.MAJBURIYAT,
                                        rw.MUKOFOT,
                                        rw.OBEKT_SONI,
                                        rw.FRAN_SM,
                                        v_ins_div,
                                        2,
                                        v_date_reg,
                                        v_val_kurs,
                                        rw.MAJBURIYAT_SUM);
                            END IF;
                        END LOOP;
                ELSE
                    -- resident
                    for pasd in (select *
                                 from table (GET_PASSPORT_BIRTH_DATE(
                                                     BIRTH_DATE => TO_DATE(SUBSTR(insured_people(i).DATE_BIRTH, 1, 10), 'YYYY-MM-DD'),
                                                     PASS_SERY => insured_people(i).PASS_SERIES,
                                                     PASS_NUM => insured_people(i).PASS_NUMBER)))
                        loop
                            if pasd.ERROR != 0 then
                                ROLLBACK;
                                out_error := -5;
                                out_error_text :=
                                        'No identity information was found through Passport data. (Birth date:' ||
                                        insured_people(i).DATE_BIRTH || ' Passport serial:' ||
                                        insured_people(i).PASS_SERIES ||
                                        ' Passport number:' || insured_people(i).PASS_NUMBER || ')';
                                return out_error;
                            end if;

                            v_client_id := FOR_TRAVEL_API_UPDATED.INSERT_TRAVELER(V_PINFL => pasd.PINFL,
                                                                                  V_PASS_SERY => insured_people(i).PASS_SERIES,
                                                                                  V_PASS_NUM => insured_people(i).PASS_NUMBER,
                                                                                  V_SURNAME => nvl(pasd.LAST_NAME_ENG, pasd.LAST_NAME),
                                                                                  V_GIVEN_NAME => nvl(pasd.FIRST_NAME_ENG, pasd.FIRST_NAME),
                                                                                  V_PATRONYMIC => nvl(pasd.MIDDLE_NAME, 'XXX'),
                                                                                  V_BIRTH_DAY => insured_people(i).DATE_BIRTH,
                                                                                  V_PASS_GIVEN => pasd.ISSUED_BY,
                                                                                  V_PASS_GIVEN_DATE => to_char(pasd.END_DATE, 'yyyy-mm-dd'),
                                                                                  V_RESIDENT => insured_people(i).IS_RESIDENT,
                                                                                  V_GENDER => insured_people(i).GENDER,
                                                                                  V_PHONE => insured_people(i).PHONE,
                                                                                  V_CITIZENSHIP => 210,
                                                                                  V_REGION => nvl(pasd.REGION_ID, 10),
                                                                                  V_DISTRICT => nvl(pasd.DISTRICT_ID, 1010),
                                                                                  V_ADDRESS => nvl(pasd.ADDRESS, '---'),
                                                                                  V_USER_ID => userId,
                                                                                  error_text => out_error_text,
                                                                                  error_code => out_error);
                            if out_error != 0 then
                                ROLLBACK;
                                return out_error;
                            end if;
                            select INS_TRAVEL_SEQ.NEXTVAL into travel_id from dual;
                            INSERT INTO INS_TRAVEL
                            (INS_ID,
                             ANKETA_ID,
                             OTV,
                             PREM,
                             PREM_MIN,
                             PREM_MAX,
                             YEARS,
                             KOEF,
                             SHENGEN,
                             CLIENT_ID, DATEBIRTH)
                            VALUES (travel_id,
                                    v_contract_id,
                                    insured_people(i).LIABILITY,
                                    insured_people(i).PREM,
                                    insured_people(i).PREM_MIN,
                                    insured_people(i).PREM_MAX,
                                    insured_people(i).AGE,
                                    insured_people(i).RATE,
                                    v_shengen,
                                    v_client_id, SUBSTR(insured_people(i).DATE_BIRTH, 1, 10));

                            --************************************INS_PSUGURTA_PO_OBYEKTAM******************************************
                            rate := insured_people(i).PREM / insured_people(i).LIABILITY * 100;
                            select count(1)
                            into cnt
                            from INS_PSUGURTA_PO_OBYEKTAM
                            where ANKETA_ID = v_contract_id
                              AND AVTO_ID = travel_id;

                            if cnt = 0 then
                                INSERT INTO INS_PSUGURTA_PO_OBYEKTAM (INS_ID,
                                                                      ANKETA_ID,
                                                                      AVTO_ID,
                                                                      LINK_ID,
                                                                      PTURI_ID,
                                                                      DIVISION_ID,
                                                                      VAL_TYPE,
                                                                      VAL_DATE,
                                                                      VAL_KURS,
                                                                      MUKOFOT,
                                                                      MUKOFOT_F,
                                                                      MAJBURIYAT,
                                                                      OBEKT_SONI,
                                                                      MUKOFOT2)
                                VALUES (INS_PSUGURTA_PO_OBYEKTAM_SEQ.nextval,
                                        v_contract_id,
                                        travel_id,
                                        3,
                                        17,
                                        v_ins_div,
                                        2,
                                        v_date_reg,
                                        v_val_kurs,
                                        insured_people(i).PREM,
                                        rate,
                                        insured_people(i).LIABILITY,
                                        1,
                                        round(insured_people(i).PREM * v_val_kurs));
                            else
                                update INS_PSUGURTA_PO_OBYEKTAM
                                set VAL_DATE   = v_date_reg,
                                    VAL_KURS   = v_val_kurs,
                                    MUKOFOT    = v_prem_usd,
                                    MUKOFOT2   = v_prem_uzs,
                                    MAJBURIYAT = v_liability
                                where ANKETA_ID = v_contract_id
                                  and AVTO_ID = travel_id
                                  and LINK_ID = 3;
                            end if;


                            --************************************INS_PSUGURTA******************************************
                            FOR rw IN (select po.LINK_ID                      as
                                                  LINK_ID,
                                              sum(po.MAJBURIYAT)              as
                                                  MAJBURIYAT,
                                              sum(po.MUKOFOT)                 as
                                                  MUKOFOT,
                                              sum(po.OBEKT_SONI)              as
                                                  OBEKT_SONI,
                                              sum(po.FRAN_SM)                 as
                                                  FRAN_SM,
                                              sum(po.MAJBURIYAT * v_val_kurs) as
                                                  MAJBURIYAT_SUM
                                       from INS_PSUGURTA_PO_OBYEKTAM po
                                       where po.ANKETA_ID = v_contract_id
                                       group by po.LINK_ID
                                       order by po.LINK_ID)
                                LOOP
                                    select count(1)
                                    INTO cnt
                                    from INS_PSUGURTA
                                    where ANKETA_ID = v_contract_id
                                      and LINK_ID = rw.LINK_ID;

                                    IF
                                        cnt > 0 THEN
                                        UPDATE INS_PSUGURTA
                                        SET MAJBURIYAT=rw.MAJBURIYAT,
                                            MUKOFOT=rw.MUKOFOT,
                                            OBEKT_SONI=rw.OBEKT_SONI,
                                            FRAN_SM=rw.FRAN_SM,
                                            VAL_TYPE = 2,
                                            VAL_DATE = v_date_reg,
                                            VAL_KURS = v_val_kurs,
                                            MAJBURIYAT_SUM = rw.MAJBURIYAT_SUM
                                        where ANKETA_ID = v_contract_id
                                          and LINK_ID = rw.LINK_ID;
                                    ELSE
                                        INSERT INTO INS_PSUGURTA (INS_ID,
                                                                  ANKETA_ID,
                                                                  PTURI_ID,
                                                                  LINK_ID,
                                                                  MAJBURIYAT,
                                                                  MUKOFOT,
                                                                  OBEKT_SONI,
                                                                  FRAN_SM,
                                                                  DIVISION_ID,
                                                                  VAL_TYPE,
                                                                  VAL_DATE,
                                                                  VAL_KURS,
                                                                  MAJBURIYAT_SUM)
                                        VALUES (INS_PSUGURTA_SEQ.nextval,
                                                v_contract_id,
                                                17,
                                                rw.LINK_ID,
                                                rw.MAJBURIYAT,
                                                rw.MUKOFOT,
                                                rw.OBEKT_SONI,
                                                rw.FRAN_SM,
                                                v_ins_div,
                                                2,
                                                v_date_reg,
                                                v_val_kurs,
                                                rw.MAJBURIYAT_SUM);
                                    END IF;
                                END LOOP;
                        end loop;
                end if;

            END LOOP;

        -- get insurance liability based on the program id


        --************************************INS_ANKETA******************************************
        SELECT sum(MAJBURIYAT), sum(MUKOFOT), sum(MAJBURIYAT_SUM)
        INTO sumOTV, sumPREM, sumOTV_SUM
        FROM INS_PSUGURTA
        WHERE ANKETA_ID = v_contract_id;


        UPDATE INS_ANKETA
        SET INS_OTV      = sumOTV,
            INS_OTV_SUM  = sumOTV_SUM,
            INS_PREM     = sumPREM,
            INS_PROGRESS = null
        WHERE INS_ID = v_contract_id;

--         v_premium_sum := round(sumPREM * v_val_kurs);

        -- ******************* INS_OPLATA *********************
        if v_prem_usd > 0 and v_val_kurs > 0 then
            select count(*) into cnt from INS_OPLATA where ANKETA_ID = v_contract_id;

            commision := F_INS_GETKOMMIS(userId, 17, v_date_reg);
            sogl := F_INS_GETSOGL(userId, v_date_reg);


            if cnt = 0 then
                insert into INS_OPLATA (INS_ID,
                                        ANKETA_ID,
                                        DIVISION_ID,
                                        USER_ID,
                                        OPL_DATA,
                                        OPL_SUMMA,
                                        INS_TYPE,
                                        OPL_TYPE,
                                        VAL_TYPE,
                                        STATUS,
                                        VAL_KURS,
                                        OPLATA,
                                        OPL_VAL,
                                        KOMMIS_F,
                                        KOMMIS_SUMMA,
                                        SOGL_ID,
                                        AGENCY_TRANSACTION_ID)
                values (INS_OPLATA_SEQ.nextval,
                        v_contract_id,
                        v_ins_div,
                        userId,
                        v_date_reg,
                        sumPREM,
                        17,
                        3,
                        2,
                        0,
                        v_val_kurs,
                        v_prem_uzs,
                        1,
                        commision,
                        -- FLOOR(v_premium_sum * commision / 100),
                        round(v_prem_uzs * commision / 100, 2),
                        sogl,
                        transaction_id);
            else
                update ins_oplata
                set OPL_DATA  = v_date_reg,
                    OPL_SUMMA = sumPREM,
                    VAL_KURS  = v_val_kurs,
                    OPLATA    = round(sumPREM * v_val_kurs,2),
                    OPL_TYPE  = 3
                where ANKETA_ID = v_contract_id;
            end if;

        end if;
        --ankprem>0

        --************************************INS_POLICY******************************************
        policy_series := 'EIND';
        SELECT INS_POLIS_BASIS.nextval * (-1) INTO policy_number FROM Dual;
        policy_id := ADD_POLIS(policy_series, policy_number, v_ins_div, userId);

        begin
            select INS_ID, OPL_DATA, OPL_TYPE, STATUS_PAYMENT, OPLATA
            into payment_id, payment_date, v_payment_type, v_payment_status, v_oplata
            from INS_OPLATA
            where ANKETA_ID = v_contract_id
              and OPL_TYPE <> 7
              and rownum = 1;
        exception
            when no_data_found then
                raise_application_error(-20404, 'Payment does not exist!');
        end;

        select max(SHENGEN) into shn from INS_TRAVEL where ANKETA_ID = v_contract_id;

        if shn = 1 and days < 92 then
            v_end_date := v_end_date + 15;
        else
            v_end_date := v_end_date;
        end if;

        for rPSUGURTA IN (SELECT * FROM INS_PSUGURTA WHERE ANKETA_ID = v_contract_id)
            LOOP
                INSERT INTO INS_POLIS_SUGURTA
                (ins_id,
                 ANKETA_ID,
                 PTURI_ID,
                 LINK_ID,
                 MAJBURIYAT,
                 MUKOFOT_F,
                 MUKOFOT,
                 OBEKT_SONI,
                 FRAN,
                 FRAN_CS,
                 FRAN_PR,
                 FRAN_SM,
                 DIVISION_ID,
                 DATA_BEGIN,
                 DATA_END,
                 DATA_VIDACHA,
                 POLIS_POOBYEKTAM,
                 POLIS_POOPLATE,
                 POLIS_SERY,
                 POLIS_NUMBER,
                 POLIS_ID,
                 VAL_TYPE,
                 VAL_DATE,
                 VAL_KURS)
                VALUES (INS_POLIS_SUGURTA_seq.nextval,
                        rPSUGURTA.ANKETA_ID,
                        rPSUGURTA.PTURI_ID,
                        rPSUGURTA.LINK_ID,
                        rPSUGURTA.MAJBURIYAT,
                        rPSUGURTA.MUKOFOT_F,
                        rPSUGURTA.MUKOFOT,
                        rPSUGURTA.OBEKT_SONI,
                        rPSUGURTA.FRAN,
                        rPSUGURTA.FRAN_CS,
                        rPSUGURTA.FRAN_PR,
                        rPSUGURTA.FRAN_SM,
                        rPSUGURTA.DIVISION_ID,
                        startDate,
                        v_end_date,
                        payment_date,
                        0,
                        0,
                        policy_series,
                        policy_number,
                        policy_id,
                        v_val_type,
                        payment_date,
                        v_val_kurs);
            END LOOP;

        select sum(MAJBURIYAT),
               sum(MUKOFOT),
               sum(FRAN_SM)
        INTO psumma,pprem,pfran
        from INS_POLIS_SUGURTA
        where POLIS_ID = policy_id;

        UPDATE INS_POLIS
        SET TB_DATE_BEGIN   = startDate,
            TB_DATE_END     = v_end_date,
            TB_RASTORG_DATE = v_end_date,
            TB_ANKETA       = v_contract_id,
            TB_SUMMA        = psumma,
            TB_PREMIA       = pprem,
            TB_FRANSHIZA    = pfran,
            TB_DATEPRINT    = sysdate,
            TB_DATECONTROL  = payment_date,
            TB_USER         = userId,
            TB_AVTO         = null,
            VAL_TYPE        = v_val_type,
            VAL_DATE        = payment_date,
            VAL_KURS        = v_val_kurs
        WHERE TB_ID = policy_id;

        if (v_create_contract_first = 1) then
            v_result_fond := ERSP_VOLUNTARY_INTEGRATIONS.CONTRACT(userId,
                                                                  v_contract_id,
                                                                  policy_id,
                                                                  0,
                                                                  out_error,
                                                                  out_error_text);
        end if;

        if out_error != 0 then
            ROLLBACK;
            return out_error;
        end if;

-- NN
      begin
        select FOND_UID into policy_uuid 
            from INS_POLIS 
            where TB_ANKETA = v_contract_id 
              and FOND_UID is not null 
              and rownum = 1;
        exception
            when others then
                begin
                    select "POLICY_UUID" into policy_uuid 
                    from POLICY_UUID 
                    where ANKETA_ID = v_contract_id 
                      and rownum = 1;
                exception
                    when others then
                        policy_uuid := null;
                end;
        end;

        begin
            select nvl(KOMMIS_SUMMA, round(OPLATA * nvl(KOMMIS_F, 0) / 100, 2))
            into commission
            from INS_OPLATA
            where ANKETA_ID = v_contract_id and rownum = 1;
        exception
            when others then
                commission := null;
        end;


        return v_contract_id;
    exception
        when others THEN
            OUT_ERROR := sqlcode;
            ASBT_SYS.INS_ERR('Travel error: ' || dbms_utility.format_error_backtrace ||
                             dbms_utility.format_error_stack);
            OUT_ERROR_TEXT := DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
            return -999;
    END CREATE_CONTRACT_FIZ_V2;

    function POLICY_ANNULMENT(
        contract_id in number,
        reason_number in number,
        reason_text in varchar2,
        error out number,
        error_text out varchar2
    ) RETURN number is
        v_result      number;
        v_polis_id    number;
        v_user_id     number;
        v_pturi_id    number;
        v_akt         number;
        v_akt_num     number;
        v_begin_date  date;
        v_ersp_status number;
        v_res_make    number;
        v_premium     number;
    BEGIN

        begin
            select p.TB_ID, o.USER_ID, o.INS_TYPE, o.AKT, p.TB_DATE_BEGIN, p.ERSP_STATUS, o.OPLATA
            into v_polis_id, v_user_id, v_pturi_id, v_akt, v_begin_date, v_ersp_status, v_premium
            from INS_POLIS p
                     left join INS_OPLATA o on o.ANKETA_ID = p.TB_ANKETA
            where p.TB_ANKETA = contract_id;

        EXCEPTION
            when NO_DATA_FOUND THEN
                error := 400;
                error_text := 'Contract not found';
                return error;
        end;

        if v_ersp_status is not null then
            error := 400;
            error_text := 'Policy already annulment';
            return error;
        end if;

        if v_begin_date < trunc(SYSDATE) then
            error := 400;
            error_text := 'Policy start date must not less than today';
            return error;
        end if;

        if v_akt is not null then
            select AKT_NUM into v_akt_num from INS_AGENT_AKT where INS_ID = v_akt;
            error := 400;
            error_text := 'Akt exist, akt number: ' || v_akt_num;
            return error;
        end if;

        if v_user_id in (2544, 1160) and v_pturi_id = 17 then -- 2379 -- old_user_id
            MAKEANNUL(v_polis_id, v_res_make);
            v_result := ersp_voluntary_integrations.POLICY_TERMINATION(
                    p_policy_id => v_polis_id,
                    p_reason => case to_number(reason_number)
                                    when 0 then reason_text
                                    else get_ersp_status(to_number(reason_number)) end,
                    p_termination_date => sysdate,
                    p_refund_amount => v_premium,
                    p_user_id => v_user_id,
                    p_error_code => error,
                    p_error_message => error_text);
            IF error = 0 THEN

                UPDATE ins_polis p
                SET p.ERSP_STATUS      = 1,
                    p.ERSP_REASON      = (case to_number(reason_number)
                                              when 0 then reason_text
                                              else get_ersp_status(to_number(reason_number)) end),
                    p.ERSP_MOTION_USER = v_user_id
                WHERE p.tb_id = v_polis_id
--                   AND p.tb_datecontrol = to_date(:P11_CHDATE, 'DD.MM.YYYY')
                  AND EXISTS (SELECT 1
                              FROM ins_polis_history ph
                              WHERE ph.tb_polis = p.tb_id
                                AND ph.tb_user IS NOT NULL
                                AND ph.tb_status = 10);
                DBMS_OUTPUT.PUT_LINE('Terminate: ' || v_res_make);
            ELSE
                UPDATE ins_polis
                SET ERSP_STATUS = 0,
                    ERSP_REASON = 'Cancellation error'
                WHERE tb_id = v_polis_id;

                return -1;
            END IF;

            COMMIT;
        else
            error := 400;
            error_text := 'You have no permission';
            return error;
        end if;

        error := 0;
        error_text := 'Successfully done!';
        return v_result;
    END POLICY_ANNULMENT;

    function create_policy(
        startDate in date,
        days in number,
        travel_type in number,
        multiId in number,
        programId in number,
        activityId in number,
        groupId in number,
        countries in varchar2,
        app_resident in number,
        app_citizenship in number,
        app_firstname in varchar2,
        app_lastname in varchar2,
        app_middlename in varchar2,
        app_email in varchar2,
        app_gender in number,
        app_Date_Birth in date,
        app_Pass_Series in varchar2,
        app_Pass_Num in varchar2,
        app_Phone in varchar2,
        userId in number,
        people in INSURED_PERSON_TRAVEL,
        policy_series in out varchar2,
        policy_number in out number,
        policy_id in out number,
        policy_uuid in out varchar2,
        contract_uuid in out varchar2,
        out_error in out number,
        out_error_text in out varchar2
    ) return number is
        contract_id number := -1;
        v_transaction_id varchar2(4000);
        v_commission     number;
        v_uuid           varchar2(4000);
    begin
        contract_id := FOR_TRAVEL_API_UPDATED.CREATE_CONTRACT_FIZ_V2(
                startDate => startDate,
                days => days,
                travel_type => travel_type,
                multiId => multiId,
                programId => programId,
                activityId => activityId,
                groupId => groupId,
                countries => countries,
                app_resident => app_resident,
                app_citizenship => app_citizenship,
                app_firstname => app_firstname,
                app_lastname => app_lastname,
                app_middlename => app_middlename,
                app_email => app_email,
                app_gender => app_gender,
                app_Date_Birth => app_Date_Birth,
                app_Pass_Series => app_Pass_Series,
                app_Pass_Num => app_pass_num,
                app_Phone => app_phone,
                userId => userId,
                people => people,
                transaction_id => v_transaction_id,
                commission => v_commission,
                policy_uuid => v_uuid,
                out_error => out_error,
                out_error_text => out_error_text
                       );

        if (out_error <> 0) then
            return -1;
        end if;

        policy_id := Generate_Policy_v2(
                contract_id => contract_id,
                policy_series => policy_series,
                policy_number => policy_number,
                error => out_error,
                error_text => out_error_text,
                policy_uuid => policy_uuid
                     );

        if (out_error = 0) then
            select uuid
            into contract_uuid
            from ins_anketa
            where INS_ID = contract_id;
        end if;

        return contract_id;
    end;

    function Insert_Kontragent_FIZ(
        v_pinfl varchar2,
        v_pass_sery varchar2,
        v_pass_num varchar2,
        v_surname varchar2,
        v_given_name varchar2,
        v_patronymic varchar2,
        v_birth_day date,
        v_pass_given varchar2,
        v_pass_given_date varchar2,
        v_resident number,
        v_email varchar2,
        v_gender number,
        v_country_id number,
        v_region number,
        v_district number,
        v_address varchar2,
        v_phone varchar2,
        v_user_id number,
        v_fiz_yur number,
        v_inn number,
        v_orgname varchar2,
        v_orgmfo number
    ) RETURN NUMBER IS

        v_kont_id number;

    begin
        if v_fiz_yur = 0 then
            BEGIN
                select nvl(k.tb_id, 0)
                into v_kont_id
                from ins_kontragent k
                where k.tb_surname = v_surname
                  and k.tb_name = v_given_name
                  and k.tb_patronym = v_patronymic
                  and k.tb_inps = v_pinfl
                  and k.tb_paspsery = v_pass_sery
                  and k.tb_paspnumber = v_pass_num
                  and k.tb_phone1 = v_phone
                  and k.TB_EMAIL = v_email
                  and ROWNUM = 1;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_kont_id := 0;
            END;
        else
            BEGIN
                select nvl(k.tb_id, 0)
                into v_kont_id
                from ins_kontragent k
                where k.tb_orginn = v_inn
                  and k.TB_ORGNAME = v_orgname
                  and k.TB_ORGMFO = v_orgmfo
                  and ROWNUM = 1
                order by k.TB_ID;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_kont_id := 0;
            END;
        end if;

        if v_kont_id = 0 then
            select "INS_KONTRAGENT_SEQ".nextval into v_kont_id from Dual;
            insert into ins_kontragent(tb_id,
                                       tb_masterid,
                                       tb_surname,
                                       tb_name,
                                       tb_patronym,
                                       tb_fizyur,
                                       tb_datebirth,
                                       tb_paspsery,
                                       tb_paspnumber,
                                       tb_paspvidan,
                                       tb_paspdate,
                                       tb_rezident,
                                       tb_email,
                                       tb_sex,
                                       tb_phone1,
                                       tb_country,
                                       tb_oblast,
                                       tb_rayon,
                                       tb_ulica,
                                       user_id,
                                       mod_user,
                                       tb_inps,
                                       tb_orginn,
                                       tb_orgname,
                                       tb_orgmfo)
            values (v_kont_id,
                    v_kont_id,
                    v_surname,
                    v_given_name,
                    v_patronymic,
                    v_fiz_yur,
                    v_birth_day,
                    v_pass_sery,
                    v_pass_num,
                    v_pass_given,
                    v_pass_given_date,
                    v_resident,
                    v_email,
                    v_gender,
                    v_phone,
                    v_country_id, -- to do
                    v_region,
                    v_district,
                    v_address,
                    v_user_id,
                    v_user_id,
                    v_pinfl,
                    v_inn,
                    v_orgname,
                    v_orgmfo);
            COMMIT;
        else
            update INS_KONTRAGENT
            set TB_OBLAST   = v_region,
                TB_RAYON    = v_district,
                TB_ULICA    = v_address,
                TB_REZIDENT = v_resident,
                TB_COUNTRY  = v_country_id
            where tb_id = v_kont_id;
        end if;

        RETURN (v_kont_id);

    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error(-1, DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' ||
                                        DBMS_UTILITY.FORMAT_ERROR_BACKTRACE());
            DBMS_OUTPUT.PUT_LINE(DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE());
            RETURN -1;
    end;

    function Generate_Policy_v2(
        contract_id in number,
        policy_series out varchar2,
        policy_number out number,
        error out number,
        error_text out varchar2,
        policy_uuid out varchar2
    ) RETURN number is
        rPSUGURTA         INS_PSUGURTA%ROWTYPE;
        v_division        number;
        v_val_type        number;
        v_val_kurs        number;
        psumma            number;
        pprem             number;
        pfran             number;
        payment_id        number;
        payment_date      date;
        v_date_begin      date;
        v_date_end        date;
        ankPrem           number;
        kurs              number;
        policy_id         number;
        shn               number := 0;
        days              number;
        policy_count      number;
        phone_number      varchar2(40);
        V_SMS_TEXT        varchar2(4000);
        v_result          varchar2(32000);
        v_user_id         number;
        v_is_online_pym   number;
        v_payment_type    number;
        v_payment_status  number;
        v_oplata          number;
        v_send_napp_first number(1);
    begin
        error := 0;
        error_text := 'Completed successfully';

        begin
            select nvl(INS_PREM, 0),
                   nvl(VAL_KURS, f_ins_getkurs(VAL_TYPE, INS_DATE)),
                   INS_DAY,
                   VAL_KURS,
                   VAL_TYPE,
                   ins_datef,
                   ins_datet,
                   USER_ID
            into
                ankprem,
                kurs,
                days,
                v_val_kurs,
                v_val_type,
                v_date_begin,
                v_date_end,
                v_user_id
            from INS_ANKETA
            where INS_ID = contract_id;

            v_division := GETUSERDIV(v_user_id);

            select nvl(SEND_NAPP_FIRST, 0)
            into v_send_napp_first
            from TB_USERS
            where tb_id = v_user_id;

        exception
            when no_data_found then
                raise_application_error(-20404, 'Contract doest not exist!');
                return -1;
        end;


        if ankprem > 0 and kurs > 0 then

            select count(1)
            into policy_count
            from INS_POLIS p
            where tb_anketa = contract_id
              and TB_STATUS = 2
              and TB_STATUS in (2, 9, 10);

            if policy_count = 0 then
                begin
                    select TB_ID into policy_id from INS_POLIS where TB_ANKETA = contract_id;
                exception
                    when others then
                        raise_application_error(-20503,
                                                'If contract created with version 1 then policy should be derived from version 1 payment!');
                end;

                begin
                    select INS_ID, OPL_DATA, OPL_TYPE, STATUS_PAYMENT, OPLATA
                    into payment_id, payment_date, v_payment_type, v_payment_status, v_oplata
                    from INS_OPLATA
                    where ANKETA_ID = contract_id
                      and OPL_TYPE <> 7
                      and rownum = 1;
                exception
                    when no_data_found then
                        raise_application_error(-20404, 'Payment does not exist!');
                end;

                UPDATE INS_OPLATA
                SET POLIS_ID   = policy_id,
                    POLIS_SERY = 'EIND'
                WHERE INS_ID = payment_id;

                /*
                select s.sp_online
                into v_is_online_pym
                from sp_typepl s
                where s.sp_id = v_payment_type;

                if v_is_online_pym = 1 and v_payment_status = 2 then
                    update INS_ANKETA
                    set IS_CHECK   = 1,
                        CHECK_USER = 1,
                        CHECK_DATE = sysdate
                    where INS_ID = contract_id;
                    commit;

                    for_1c_api.Payment_by_Online_Bc(v_oplata_id => payment_id,
                                                    v_oplata => v_oplata,
                                                    v_opl_date => payment_date,
                                                    v_div_id => v_division);

                    for_1c_api.Filling_BC_1C(v_opl_id => payment_id);

                    begin
                        update INS_BANK_CLIENT
                        set STATUS = 2
                        where DOC_NUM = payment_id;
                    end;
                end if;
                */

                if (v_send_napp_first = 1) then
                    UPDATE_POLICY_WITH_UUID(contract_id, policy_id, error, error_text);
                    commit;
                    v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONFIRM_PAYED(P_USER_ID => v_user_id,
                                                                          P_CONTRACT_ID => contract_id,
                                                                          P_POLICY_ID => policy_id,
                                                                          P_REQ_ID => 0,
                                                                          P_ERROR_CODE => error,
                                                                          P_ERROR_MESSAGE => error_text);
                    if error != 0 then
                        ROLLBACK;

                        UPDATE INS_POLIS
                        SET TB_STATUS = 1
                        WHERE TB_ID = policy_id;

                        -- return error_text;
                         return error;
                    end if;
                else

                    v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONTRACT(P_USER_ID => v_user_id,
                                                                     P_CONTRACT_ID => contract_id,
                                                                     P_POLICY_ID => policy_id,
                                                                     REQ_ID => 0,
                                                                     P_ERROR_CODE => error,
                                                                     P_ERROR_MESSAGE => error_text);
                    if error != 0 then
                        ROLLBACK;
                        update INS_POLIS SET TB_STATUS = 1 WHERE TB_ID = policy_id;
                        return error;
                    end if;
                    v_result := ERSP_VOLUNTARY_INTEGRATIONS.CONFIRM_PAYED(P_USER_ID => v_user_id,
                                                                          P_CONTRACT_ID => contract_id,
                                                                          P_POLICY_ID => policy_id,
                                                                          P_REQ_ID => 0,
                                                                          P_ERROR_CODE => error,
                                                                          P_ERROR_MESSAGE => error_text);
                    if error != 0 then
                        ROLLBACK;
                        update INS_POLIS SET TB_STATUS = 1 WHERE TB_ID = policy_id;
                        return error;
                    end if;
                end if;

                UPDATE INS_POLIS
                SET TB_STATUS = 2
                where TB_ID = policy_id;

                select TB_SERY, TB_NUMBER, FOND_UID
                into policy_series,policy_number,policy_uuid
                from INS_POLIS
                where TB_ID = policy_id;

                update ins_oplata
                set POLIS_ID     = policy_id,
                    POLIS_SERY   = policy_series,
                    POLIS_NUMBER = policy_number,
                    opl_data     = sysdate
                where ANKETA_ID = contract_id;

                --- *** --- updated: 16.02.2026 Sardor Urokov
                select s.sp_online
                into v_is_online_pym
                from sp_typepl s
                where s.sp_id = v_payment_type;

                if v_is_online_pym = 1 and v_payment_status = 2 then
                    update INS_ANKETA
                    set IS_CHECK     = 1,
                        CHECK_USER   = 1,
                        INS_STAT=2,
                        INS_PROGRESS = null,
                        CHECK_DATE   = sysdate
                    where INS_ID = contract_id;
                    commit;

                    for_1c_api.Payment_by_Online_Bc(v_oplata_id => payment_id,
                                                    v_oplata => v_oplata,
                                                    v_opl_date => payment_date,
                                                    v_div_id => v_division);
                    for_1c_api.Filling_BC_1C(v_opl_id => payment_id);

                    begin
                        update INS_BANK_CLIENT
                        set STATUS = 2
                        where DOC_NUM = payment_id;
                    end;
                end if;
                --- *** ---
                UPDATE INS_ANKETA
                SET INS_STAT=2,
                    INS_PROGRESS = null
                WHERE INS_ID = contract_id;

                /******************************** ОТПРАВКА СМС ********************************/
                select REGEXP_REPLACE(COALESCE(k.TB_PHONE1, k.TB_PHONE2, k.TB_PHONE3), '[^0-9]', '')
                into phone_number
                from ins_kontragent k
                         inner join INS_ANKETA a on a.owner = k.tb_id
                where a.ins_id = contract_id;

                V_SMS_TEXT :=
                        'Polis faol. Amal qilish muddati: ' || to_char(v_date_end, 'DD.MM.YYYY') || ' ' || CHR(10) ||
                        'Sayohatingizdan rohatlaning!' || CHR(10) ||
                        'Ваш полис активен. Срок действия: ' || to_char(v_date_end, 'DD.MM.YYYY') || ' ' || CHR(10) ||
                        'Приятного путешествия!' || CHR(10) ||
                        'https://api-travel.insonline.uz/api/travel/policy-old/' || policy_id || '/' || contract_id;
                /******************************************************************************/
            else
                select tb_sery, tb_number, tb_id, FOND_UID
                into policy_series, policy_number, policy_id, policy_uuid
                from ins_polis
                where TB_ANKETA = contract_id;
                return policy_id;
            end if;
        end if;
        return policy_id;

    exception
        when others then
            rollback;
            error := sqlcode;
            error_text := DBMS_UTILITY.FORMAT_ERROR_STACK() || ' ; ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE();
            return -1;
    end;

    procedure set_program_name_to_polis(p_anketa_id in number)
        is
        p_ins_program_id number;
        v_program_name   varchar2(100);
    begin
        select abroad_program
        into p_ins_program_id
        from ins_anketa
        where ins_id = p_anketa_id;

        select 'Sug‘urta dasturi: ' || nvl(iap.program_name, '')
        into v_program_name
        from ins_abroad_program iap
        where iap.ins_id = p_ins_program_id
          and iap.active = 1
          and rownum = 1;

        update ins_polis
        set note = v_program_name
        where tb_anketa = p_anketa_id;

    exception
        when no_data_found then
            null;
    end;    

end FOR_TRAVEL_API_UPDATED;
