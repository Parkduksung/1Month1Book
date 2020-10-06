package com.rsupport.mobile.agent.utils.crypto;


public class SeedAlg {

    public SeedAlg(byte key[]) {
        RoundKey = new long[32];
        seedEncRoundKey(key);
    }

    public byte[] encrypt(byte plainData[]) {
        int nLoopCnt = plainData.length / 16 + (plainData.length % 16 == 0 ? 0 : 1);
        byte retArray[] = new byte[nLoopCnt * 16];
        for (int i = 0; i < nLoopCnt; i++) {
            long nTemp[] = new long[4];
            for (int j = 0; j < 4; j++)
                nTemp[j] = makeLong(plainData, i * 16 + j * 4);

            nTemp = encrypt16(nTemp);
            for (int j = 0; j < 16; j++)
                retArray[i * 16 + j] = getAByteLong(nTemp[j / 4], 3 - j % 4);

        }

        return retArray;
    }

    private long[] encrypt16(long plainData[]) {
        boolean bSwapFlag = false;
        for (int i = 0; i < 16; i++) {
            plainData = SeedRound(plainData, i, bSwapFlag);
            bSwapFlag = !bSwapFlag;
        }

        return (new long[]{
                plainData[2], plainData[3], plainData[0], plainData[1]
        });
    }


    public byte[] decrypt(byte cipherData[]) {
        int nLoopCnt = cipherData.length / 16
                + (cipherData.length % 16 == 0 ? 0 : 1);
        byte retArray[] = new byte[nLoopCnt * 16];
        for (int i = 0; i < nLoopCnt; i++) {
            long nTemp[] = new long[4];
            for (int j = 0; j < 4; j++) {
                nTemp[j] = makeLong(cipherData, i * 16 + j * 4);
            }

            nTemp = decrypt16(nTemp);
            for (int j = 0; j < 16; j++) {
                retArray[i * 16 + j] = getAByteLong(nTemp[j / 4], 3 - j % 4);
            }
        }

        return retArray;
    }

    private void seedEncRoundKey(byte pbUserKey[]) {
        long keyData[] = {makeLong(pbUserKey, 0), makeLong(pbUserKey, 4),
                makeLong(pbUserKey, 8), makeLong(pbUserKey, 12)};
        long T0 = (keyData[0] + keyData[2]) - KC[0];
        long T1 = (keyData[1] - keyData[3]) + KC[0];
        RoundKey[0] = SS0[(int) getAByteToLong(T0, 0)]
                ^ SS1[(int) getAByteToLong(T0, 1)]
                ^ SS2[(int) getAByteToLong(T0, 2)]
                ^ SS3[(int) getAByteToLong(T0, 3)];
        RoundKey[1] = SS0[(int) getAByteToLong(T1, 0)]
                ^ SS1[(int) getAByteToLong(T1, 1)]
                ^ SS2[(int) getAByteToLong(T1, 2)]
                ^ SS3[(int) getAByteToLong(T1, 3)];
        for (int i = 0; i < 7; i++) {
            keyData = encRoundKeyUpdate0(keyData, i * 2 + 1);
            keyData = encRoundKeyUpdate1(keyData, i * 2 + 2);
        }

        encRoundKeyUpdate0(keyData, 15);
    }

    private long[] decrypt16(long cipherData[]) {
        boolean bSwapFlag = false;
        for (int i = 15; i >= 0; i--) {
            cipherData = SeedRound(cipherData, i, bSwapFlag);
            bSwapFlag = !bSwapFlag;
        }

        return (new long[]{cipherData[2], cipherData[3], cipherData[0],
                cipherData[1]});
    }

    private long[] encRoundKeyUpdate0(long keyData[], int roundCnt) {
        long T0 = keyData[0];
        keyData[0] = keyData[0] >> 8 & 4294967295L ^ keyData[1] << 24
                & 4294967295L;
        keyData[1] = keyData[1] >> 8 & 4294967295L ^ T0 << 24 & 4294967295L;
        T0 = (keyData[0] + keyData[2]) - KC[roundCnt] & 4294967295L;
        long T1 = (keyData[1] + KC[roundCnt]) - keyData[3] & 4294967295L;
        RoundKey[roundCnt * 2] = SS0[(int) getAByteToLong(T0, 0)]
                ^ SS1[(int) getAByteToLong(T0, 1)]
                ^ SS2[(int) getAByteToLong(T0, 2)]
                ^ SS3[(int) getAByteToLong(T0, 3)];
        RoundKey[roundCnt * 2 + 1] = SS0[(int) getAByteToLong(T1, 0)]
                ^ SS1[(int) getAByteToLong(T1, 1)]
                ^ SS2[(int) getAByteToLong(T1, 2)]
                ^ SS3[(int) getAByteToLong(T1, 3)];
        return keyData;
    }

    private long[] encRoundKeyUpdate1(long keyData[], int roundCnt) {
        long T0 = keyData[2];
        keyData[2] = (keyData[2] << 8 ^ keyData[3] >> 24) & 4294967295L;
        keyData[3] = (keyData[3] << 8 ^ T0 >> 24) & 4294967295L;
        T0 = (keyData[0] + keyData[2]) - KC[roundCnt] & 4294967295L;
        long T1 = (keyData[1] + KC[roundCnt]) - keyData[3] & 4294967295L;
        RoundKey[roundCnt * 2] = SS0[(int) getAByteToLong(T0, 0)]
                ^ SS1[(int) getAByteToLong(T0, 1)]
                ^ SS2[(int) getAByteToLong(T0, 2)]
                ^ SS3[(int) getAByteToLong(T0, 3)];
        RoundKey[roundCnt * 2 + 1] = SS0[(int) getAByteToLong(T1, 0)]
                ^ SS1[(int) getAByteToLong(T1, 1)]
                ^ SS2[(int) getAByteToLong(T1, 2)]
                ^ SS3[(int) getAByteToLong(T1, 3)];
        return keyData;
    }

    private byte getAByteLong(long src, int index) {
        return (byte) (int) (src >>> index * 8 & 255L);
    }

    private long getAByteToLong(long src, int index) {
        return src >> index * 8 & 255L;
    }

    private long makeLong(byte byteArray[], int nStartIndex) {
        long b1 = byteArray.length <= nStartIndex ? 0L
                : (255L & (long) byteArray[nStartIndex + 0]) << 24;
        long b2 = byteArray.length <= nStartIndex + 1 ? 0L
                : (255L & (long) byteArray[nStartIndex + 1]) << 16;
        long b3 = byteArray.length <= nStartIndex + 2 ? 0L
                : (255L & (long) byteArray[nStartIndex + 2]) << 8;
        long b4 = byteArray.length <= nStartIndex + 3 ? 0L
                : 255L & (long) byteArray[nStartIndex + 3];
        return b1 + b2 + b3 + b4;
    }

    private long[] SeedRound(long data[], int roundIndex, boolean swapFlag) {
        long L0;
        long L1;
        long R0;
        long R1;
        if (!swapFlag) {
            L0 = data[0];
            L1 = data[1];
            R0 = data[2];
            R1 = data[3];
        } else {
            R0 = data[0];
            R1 = data[1];
            L0 = data[2];
            L1 = data[3];
        }
        long T0 = R0 ^ RoundKey[roundIndex * 2];
        long T1 = R1 ^ RoundKey[roundIndex * 2 + 1];
        T1 ^= T0;
        T1 = SS0[(int) getAByteToLong(T1, 0)]
                ^ SS1[(int) getAByteToLong(T1, 1)]
                ^ SS2[(int) getAByteToLong(T1, 2)]
                ^ SS3[(int) getAByteToLong(T1, 3)];
        T0 += T1;
        T0 = SS0[(int) getAByteToLong(T0, 0)]
                ^ SS1[(int) getAByteToLong(T0, 1)]
                ^ SS2[(int) getAByteToLong(T0, 2)]
                ^ SS3[(int) getAByteToLong(T0, 3)];
        T1 += T0;
        T1 = SS0[(int) getAByteToLong(T1, 0)]
                ^ SS1[(int) getAByteToLong(T1, 1)]
                ^ SS2[(int) getAByteToLong(T1, 2)]
                ^ SS3[(int) getAByteToLong(T1, 3)];
        T0 += T1;
        L0 ^= T0;
        L1 ^= T1;
        return swapFlag ? (new long[]{R0, R1, L0, L1}) : (new long[]{L0,
                L1, R0, R1});
    }

    static long SS0[] = {696885672L, 92635524L, 382128852L, 331600848L,
            340021332L, 487395612L, 747413676L, 621093156L, 491606364L,
            54739776L, 403181592L, 504238620L, 289493328L, 1020063996L,
            181060296L, 591618912L, 671621160L, 71581764L, 536879136L,
            495817116L, 549511392L, 583197408L, 147374280L, 386339604L,
            629514660L, 261063564L, 50529024L, 994800504L, 999011256L,
            318968592L, 314757840L, 785310444L, 809529456L, 210534540L,
            1057960764L, 680042664L, 839004720L, 500027868L, 919007988L,
            876900468L, 751624428L, 361075092L, 185271048L, 390550356L,
            474763356L, 457921368L, 1032696252L, 16843008L, 604250148L,
            470552604L, 860058480L, 411603096L, 268439568L, 214745292L,
            851636976L, 432656856L, 738992172L, 667411428L, 843215472L,
            58950528L, 462132120L, 297914832L, 109478532L, 164217288L,
            541089888L, 272650320L, 595829664L, 734782440L, 218956044L,
            914797236L, 512660124L, 256852812L, 931640244L, 441078360L,
            113689284L, 944271480L, 646357668L, 302125584L, 797942700L,
            365285844L, 557932896L, 63161280L, 881111220L, 21053760L,
            306336336L, 1028485500L, 227377548L, 134742024L, 521081628L,
            428446104L, 0, 420024600L, 67371012L, 323179344L, 935850996L,
            566354400L, 1036907004L, 910586484L, 789521196L, 654779172L,
            813740208L, 193692552L, 235799052L, 730571688L, 578986656L,
            776888940L, 327390096L, 223166796L, 692674920L, 1011642492L,
            151585032L, 168428040L, 1066382268L, 802153452L, 868479984L,
            96846276L, 126321540L, 335810580L, 1053750012L, 608460900L,
            516870876L, 772678188L, 189481800L, 436867608L, 101057028L,
            553722144L, 726360936L, 642146916L, 33686016L, 902164980L,
            310547088L, 176849544L, 202113036L, 864269232L, 1045328508L,
            281071824L, 977957496L, 122110788L, 377918100L, 633725412L,
            637936164L, 8421504L, 764256684L, 533713884L, 562143648L,
            805318704L, 923218740L, 781099692L, 906375732L, 352653588L,
            570565152L, 940060728L, 885321972L, 663200676L, 88424772L,
            206323788L, 25264512L, 701096424L, 75792516L, 394761108L,
            889532724L, 197903304L, 248431308L, 1007431740L, 826372464L,
            285282576L, 130532292L, 160006536L, 893743476L, 1003222008L,
            449499864L, 952692984L, 344232084L, 424235352L, 42107520L,
            80003268L, 1070593020L, 155795784L, 956903736L, 658989924L,
            12632256L, 265274316L, 398971860L, 948482232L, 252642060L,
            244220556L, 37896768L, 587408160L, 293704080L, 743202924L,
            466342872L, 612671652L, 872689716L, 834793968L, 138952776L,
            46318272L, 793731948L, 1024274748L, 755835180L, 4210752L,
            1049539260L, 1041117756L, 1015853244L, 29475264L, 713728680L,
            982168248L, 240009804L, 356864340L, 990589752L, 483184860L,
            675831912L, 1062171516L, 478974108L, 415813848L, 172638792L,
            373707348L, 927429492L, 545300640L, 768467436L, 105267780L,
            897954228L, 722150184L, 625303908L, 986379000L, 600040416L,
            965325240L, 830583216L, 529503132L, 508449372L, 969535992L,
            650568420L, 847426224L, 822161712L, 717939432L, 760045932L,
            525292380L, 616882404L, 817950960L, 231588300L, 143163528L,
            369496596L, 973746744L, 407392344L, 348442836L, 574775904L,
            688464168L, 117900036L, 855847728L, 684253416L, 453710616L,
            84214020L, 961114488L, 276861072L, 709517928L, 705307176L,
            445289112L};

    static long SS1[] = {943196208L, -399980320L, 741149985L, -1540979038L,
            -871379005L, -601960750L, -1338801229L, -1204254544L, -1406169181L,
            1612726368L, 1410680145L, -1006123069L, 1141130304L, 1815039843L,
            1747667811L, 1478183763L, -1073495101L, 1612857954L, 808649523L,
            -1271560783L, 673777953L, -1608482656L, -534592798L, -1540913245L,
            -804011053L, -1877900911L, 269549841L, 67503618L, 471600144L,
            -1136882512L, 875955762L, 1208699715L, -332410909L, -2012706688L,
            1814842464L, -1473738592L, 337053459L, -1006320448L, 336987666L,
            -197868304L, -1073560894L, 1141196097L, -534658591L, -736704814L,
            1010765619L, 1010634033L, -1945203070L, -1743222640L, 673712160L,
            1276005954L, -197736718L, 1010699826L, -1541044831L, -130430479L,
            202181889L, -601894957L, -669464368L, 673909539L, 1680229986L,
            2017086066L, 606537507L, 741281571L, -265174543L, 1882342002L,
            1073889858L, -736836400L, 1073824065L, -1073692480L, 1882407795L,
            1680295779L, -1406366560L, -2012509309L, -197670925L, -1406300767L,
            -2147450752L, 471797523L, -938816830L, 741084192L, -1473607006L,
            875824176L, -804076846L, 134941443L, -332476702L, -399914527L,
            1545424209L, -1810594672L, 404228112L, -130496272L, 1410811731L,
            -1406234974L, 134744064L, -1006254655L, 269681427L, -871510591L,
            -2079947134L, -1204188751L, -62926861L, 2084392305L, -1073626687L,
            808517937L, -197802511L, -2012575102L, 1747602018L, -1338932815L,
            -804142639L, 538968096L, -736639021L, 131586L, 539099682L,
            67372032L, 1747470432L, 1882276209L, 67569411L, -669266989L,
            -1675784815L, -1743156847L, 1612792161L, -1136750926L, -467220766L,
            1478052177L, -602026543L, 1343308113L, -1877966704L, -602092336L,
            -1743091054L, -1608285277L, -1473541213L, -804208432L,
            -2147384959L, 202313475L, 1141327683L, 404359698L, -534527005L,
            -332608288L, -1945268863L, -1136685133L, -1810463086L, 2017151859L,
            1545358416L, -1608351070L, -1608416863L, 1612923747L, 539165475L,
            1275940161L, -938948416L, -1675719022L, -1675850608L, 943327794L,
            202116096L, 741215778L, -1204122958L, 1814974050L, -1675653229L,
            1478117970L, -265108750L, -1877835118L, -265042957L, 1208568129L,
            2016954480L, -871576384L, 336921873L, -130298893L, 1882210416L,
            1949648241L, 2084523891L, 875889969L, 269484048L, 197379L,
            1680098400L, 1814908257L, -1006188862L, 1949582448L, -736770607L,
            -1271626576L, -399848734L, 134809857L, 1949714034L, 404293905L,
            -62992654L, 1073758272L, 269615634L, -534724384L, -1136816719L,
            67437825L, -130364686L, 65793L, -265240336L, 673843746L,
            1545490002L, -1473672799L, 1410745938L, 1073955651L, -2080012927L,
            336856080L, -2012640895L, -1743025261L, -1338998608L, -467286559L,
            1208502336L, 2017020273L, -1810397293L, -63124240L, 471731730L,
            -2147319166L, 539033889L, -1945334656L, 404425491L, 1545555795L,
            1949779827L, 1410614352L, -1338867022L, 471665937L, 606405921L,
            1276071747L, 0, 1141261890L, -332542495L, 1477986384L, 1343373906L,
            -399782941L, 2084458098L, -669332782L, -938882623L, -63058447L,
            808452144L, -1810528879L, 1680164193L, 1010568240L, -1271494990L,
            -467352352L, -1204057165L, 2084326512L, 202247682L, 1343242320L,
            943262001L, 606471714L, 808583730L, -2080078720L, 1747536225L,
            -1877769325L, 876021555L, -467154973L, 606340128L, -1541110624L,
            -938751037L, 1343439699L, 134875650L, -2079881341L, -669398575L,
            1275874368L, -2147253373L, -1945137277L, -871444798L, 943393587L,
            1208633922L, -1271429197L};

    static long SS2[] = {-1582814839L, -2122054267L, -757852474L, -741338173L,
            1347687492L, 287055117L, -1599329140L, 556016901L, 1364991309L,
            1128268611L, 270014472L, 303832590L, 1364201793L, -251904820L,
            -1027077430L, 1667244867L, 539502600L, 1078199364L, 538976256L,
            -1852039795L, -522182464L, -488627518L, -1060632376L, 320083719L,
            -1583078011L, -2087972977L, 50332419L, 1937259339L, -1279771765L,
            319820547L, -758115646L, -487838002L, 1886400576L, -2138305396L,
            859586319L, -1599592312L, 842019330L, -774103603L, -218876218L,
            1886663748L, -521392948L, -1852566139L, 50858763L, 1398019911L,
            1348213836L, 1398283083L, -1313063539L, 16777473L, 539239428L,
            270277644L, 1936732995L, -1869080440L, 269488128L, -1060369204L,
            -219139390L, -774366775L, 539765772L, -471586873L, 1919955522L,
            -2088762493L, -1818748021L, -774893119L, -2105276794L,
            -1043854903L, 1616912448L, 1347424320L, -1549786237L, -471323701L,
            17566989L, -1296812410L, -1835262322L, 1129058127L, -1280034937L,
            1381505610L, -1027340602L, 1886926920L, -1566300538L, 303043074L,
            -1548996721L, -774629947L, 1633689921L, -1010826301L, -1330367356L,
            1094713665L, 1380979266L, 1903967565L, -2121527923L, 526344L,
            320610063L, -1852302967L, 0, 286791945L, 263172L, 1397756739L,
            -202098745L, -505404991L, -235127347L, 1920218694L, 590098191L,
            589571847L, -1330630528L, -2088236149L, 34344462L, -1549259893L,
            -1566563710L, 1651256910L, -1819274365L, 1095503181L, 1634216265L,
            1887190092L, 17303817L, 34081290L, -1279508593L, -471060529L,
            -202361917L, -1044118075L, -2088499321L, 269751300L, -218349874L,
            1617175620L, -757326130L, 573320718L, 1128794955L, 303569418L,
            33818118L, 555753729L, 1667771211L, 1650730566L, 33554946L,
            -235653691L, -1836051838L, -2105013622L, 789516L, -1280298109L,
            1920745038L, -791670592L, 1920481866L, 1128531783L, -1835788666L,
            -505141819L, 572794374L, -2139094912L, -1582551667L, -740548657L,
            -1583341183L, 808464384L, 859059975L, -1565774194L, 842282502L,
            286528773L, 572531202L, 808990728L, -252431164L, -1549523065L,
            1094976837L, 1078725708L, -2122317439L, -504878647L, -2138831740L,
            -1819011193L, 825505029L, -1010299957L, -1026814258L, 809253900L,
            1903178049L, 286265601L, -1010563129L, -2121791095L, 1903441221L,
            -201835573L, -757589302L, -252167992L, -1869343612L, 1364728137L,
            -2105539966L, -1060895548L, -201572401L, 1095240009L, 825768201L,
            1667508039L, -1061158720L, -1010036785L, -741075001L, -1330104184L,
            51121935L, -2104750450L, 1111491138L, 589308675L, -1852829311L,
            1617701964L, -740811829L, -1599855484L, 808727556L, -235916863L,
            1078462536L, -1027603774L, 1668034383L, 826031373L, 556543245L,
            1077936192L, -1296286066L, 842808846L, -1329841012L, -1044381247L,
            -1566037366L, -1296549238L, 1112280654L, 1364464965L, 859323147L,
            -790881076L, 1617438792L, 1937522511L, -1868817268L, -791144248L,
            1112017482L, 1381242438L, 1936996167L, -1600118656L, -504615475L,
            1111754310L, -1313589883L, 589835019L, 1633953093L, -218613046L,
            -471850045L, -1313326711L, -1313853055L, -1818484849L, 1381768782L,
            -235390519L, -488364346L, -1297075582L, 825241857L, -488101174L,
            1634479437L, 1398546255L, -521919292L, -252694336L, -1043591731L,
            -2138568568L, 303306246L, 842545674L, 1347950664L, -791407420L,
            1650467394L, 556280073L, 50595591L, 858796803L, -521656120L,
            320346891L, 17040645L, 1903704393L, -1869606784L, 1650993738L,
            573057546L, -1835525494L};

    static long SS3[] = {137377848L, -924784600L, 220277805L, -2036161498L,
            -809251825L, -825041890L, -2085375949L, -2001684424L, -1885098961L,
            1080057888L, 1162957845L, -943471609L, 1145062404L, 1331915823L,
            1264805931L, 1263753243L, -1010581501L, 1113743394L, 53686323L,
            -2051951563L, 153167913L, -2136956896L, -1025318878L, -2019318745L,
            -1009528813L, -2121166831L, 17895441L, 100795398L, 202382364L,
            -1934574532L, 103953462L, 1262700555L, -807146449L, -2004842488L,
            1281387564L, -2002737112L, 118690839L, -993999868L, 101848086L,
            -990841804L, -1027424254L, 1161905157L, -1042161631L, -959261674L,
            255015999L, 221330493L, -1904047090L, -2003789800L, 136325160L,
            1312967694L, -957156298L, 238173246L, -2053004251L, -906889159L,
            218172429L, -808199137L, -925837288L, 186853419L, 1180853286L,
            1249015866L, 119743527L, 253963311L, -1041108943L, 1114796082L,
            1111638018L, -992947180L, 1094795265L, -1061109760L, 1131638835L,
            1197696039L, -1935627220L, -1954314229L, -940313545L, -1918784467L,
            -2139062272L, 252910623L, -893204470L, 203435052L, -1969051606L,
            70267956L, -1026371566L, 184748043L, -823989202L, -907941847L,
            1297177629L, -2070899692L, 135272472L, -923731912L, 1196643351L,
            -1901941714L, 134219784L, -977157115L, 51580947L, -842937331L,
            -2038266874L, -1984841671L, -806093761L, 1299283005L, -1044267007L,
            20000817L, -973999051L, -1971156982L, 1247963178L, -2119061455L,
            -1043214319L, 2105376L, -942418921L, 33685506L, 35790882L,
            67109892L, 1214277672L, 1097953329L, 117638151L, -875309029L,
            -1919837155L, -1986947047L, 1096900641L, -1900889026L, -958208986L,
            1230067737L, -841884643L, 1095847953L, -2138009584L, -858727396L,
            -1970104294L, -2086428637L, -1952208853L, -1060057072L,
            -2122219519L, 251857935L, 1195590663L, 168957978L, -1008476125L,
            -857674708L, -1920889843L, -1884046273L, -2037214186L, 1265858619L,
            1280334876L, -2103271390L, -2120114143L, 1130586147L, 52633635L,
            1296124941L, -926889976L, -1902994402L, -1936679908L, 171063354L,
            201329676L, 237120558L, -1967998918L, 1315073070L, -1886151649L,
            1246910490L, -1024266190L, -2104324078L, -1007423437L, 1229015049L,
            1215330360L, -859780084L, 85005333L, -873203653L, 1081110576L,
            1165063221L, 1332968511L, 87110709L, 1052688L, 50528259L,
            1147167780L, 1298230317L, -960314362L, 1148220468L, -976104427L,
            -2068794316L, -891099094L, 151062537L, 1181905974L, 152115225L,
            -822936514L, 1077952512L, 34738194L, -1059004384L, -1917731779L,
            83952645L, -890046406L, 16842753L, -1057951696L, 170010666L,
            1314020382L, -1985894359L, 1179800598L, 1128480771L, -2055109627L,
            68162580L, -1987999735L, -1953261541L, -2135904208L, -975051739L,
            1212172296L, 1232173113L, -2020371433L, -856622020L, 236067870L,
            -2105376766L, 18948129L, -1937732596L, 185800731L, 1330863135L,
            1198748727L, 1146115092L, -2102218702L, 219225117L, 86058021L,
            1329810447L, 0, 1178747910L, -840831955L, 1213224984L, 1112690706L,
            -874256341L, 1316125758L, -892151782L, -910047223L, -839779267L,
            3158064L, -2054056939L, 1164010533L, 204487740L, -2035108810L,
            -991894492L, -1951156165L, 1282440252L, 235015182L, 1079005200L,
            154220601L, 102900774L, 36843570L, -2071952380L, 1231120425L,
            -2087481325L, 120796215L, -941366233L, 69215268L, -2069847004L,
            -876361717L, 1129533459L, 167905290L, -2021424121L, -908994535L,
            1279282188L, -2088534013L, -1887204337L, -826094578L, 187906107L,
            1245857802L, -2018266057L};

    static long KC[] = {-1640531527L, 1013904243L, 2027808486L, -239350324L,
            -478700647L, -957401293L, -1914802585L, 465362127L, 930724254L,
            1861448508L, -572070280L, -1144140559L, 2006686179L, -281594938L,
            -563189875L, -1126379749L};

    long RoundKey[];

}