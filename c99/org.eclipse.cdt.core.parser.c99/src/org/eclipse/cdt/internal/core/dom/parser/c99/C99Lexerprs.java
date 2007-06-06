/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.parser.c99;

public class C99Lexerprs implements lpg.lpgjavaruntime.ParseTable, C99Lexersym {

    public interface IsKeyword {
        public final static byte isKeyword[] = {0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0
        };
    };
    public final static byte isKeyword[] = IsKeyword.isKeyword;
    public final boolean isKeyword(int index) { return isKeyword[index] != 0; }

    public interface BaseCheck {
        public final static byte baseCheck[] = {0,
            1,1,1,1,1,1,1,1,1,1,
            1,1,2,2,2,1,1,1,1,1,
            1,1,1,2,2,1,1,2,2,2,
            2,1,1,2,2,1,1,1,3,1,
            2,2,2,2,2,3,3,2,2,2,
            1,1,2,2,2,2,2,2,4,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            2,2,2,5,1,2,3,2,2,0,
            1,2,2,1,1,3,4,4,1,2,
            1,2,1,2,2,2,1,2,2,1,
            2,1,2,1,1,1,1,2,2,1,
            1,1,2,3,2,2,3,3,4,3,
            4,1,2,2,2,3,3,2,3,2,
            1,1,2,2,3,3,2,3,2,1,
            2,1,1,1,1,3,4,1,2,1,
            1,1,1,1,1,2,2,2,2,2,
            2,2,2,2,2,2,2,3,2,3,
            3,4,1,2,1,1
        };
    };
    public final static byte baseCheck[] = BaseCheck.baseCheck;
    public final int baseCheck(int index) { return baseCheck[index]; }
    public final static byte rhs[] = baseCheck;
    public final int rhs(int index) { return rhs[index]; };

    public interface BaseAction {
        public final static char baseAction[] = {
            31,31,31,31,31,31,31,31,31,31,
            31,31,31,31,31,31,31,31,31,31,
            31,31,31,31,31,31,31,31,31,31,
            31,31,31,31,31,31,31,31,31,31,
            31,31,31,31,31,31,31,31,31,31,
            31,31,31,31,31,31,31,31,31,31,
            2,2,2,2,2,2,2,2,2,2,
            2,2,2,2,2,2,2,2,2,2,
            2,2,2,2,2,2,2,2,2,2,
            2,2,2,2,2,2,2,2,2,2,
            2,2,2,2,2,2,2,2,2,2,
            2,2,23,23,23,1,1,1,1,1,
            1,1,1,1,1,37,37,37,37,37,
            37,37,37,3,3,3,3,3,3,3,
            3,3,3,3,3,3,4,4,4,4,
            4,4,4,4,4,4,4,4,4,4,
            4,4,4,4,4,4,4,4,4,4,
            38,38,38,38,38,38,12,12,12,12,
            12,12,12,12,12,12,13,13,13,13,
            13,13,13,13,13,13,14,14,14,14,
            39,39,39,39,39,39,39,24,24,24,
            24,24,24,24,24,24,31,31,31,31,
            40,40,41,41,42,44,44,43,43,43,
            43,32,32,32,25,25,6,6,19,33,
            33,33,33,45,46,47,47,26,26,26,
            26,26,26,26,15,15,21,21,22,22,
            34,34,48,48,48,48,48,48,49,49,
            49,49,5,5,50,50,50,27,27,27,
            27,16,16,51,51,51,28,28,28,28,
            20,20,11,11,11,11,35,35,29,29,
            17,17,7,7,7,7,8,8,8,8,
            8,8,8,8,8,8,8,9,10,36,
            36,36,36,30,30,18,18,1,577,112,
            1417,364,356,235,1207,364,395,176,177,1571,
            178,582,305,325,302,303,304,96,326,1143,
            234,1078,231,221,323,1189,1375,273,349,500,
            2,3,4,5,1428,273,425,339,492,219,
            1217,317,520,518,512,260,261,370,971,186,
            187,265,188,436,305,300,302,303,304,1096,
            485,301,683,176,177,298,178,403,305,325,
            302,303,304,1386,326,1251,133,444,318,1224,
            323,971,186,187,264,188,1271,305,300,302,
            303,304,458,1618,301,491,176,177,298,178,
            1567,305,325,302,303,304,1580,326,1319,133,
            471,291,1575,324,587,186,187,267,188,1621,
            305,300,302,303,304,1110,1622,301,779,176,
            177,299,178,1598,305,325,302,303,304,1095,
            326,875,186,187,269,188,324,305,300,302,
            303,304,1367,463,301,101,207,208,299,209,
            1446,625,299,200,201,625,170,253,1272,210,
            1067,233,112,200,207,208,235,209,1596,229,
            1468,273,1178,133,1576,290,1302,210,1568,1395,
            1570,1404,510,234,1216,232,510,227,1156,503,
            203,223,551,573,251,573,1602,592,1623,519,
            482,519,482,1624,242,1220,240,271,1404,623,
            1165,1145,133,623,291,1251,133,1144,290,1251,
            133,1626,601,455,598,1251,133,1306,601,1404,
            631,1404,633,1605,631,438,633,1265,588,536,
            1291,133,1192,290,236,605,641,608,1251,133,
            641,601,1457,627,248,249,641,627,1457,629,
            611,1251,133,629,615,1457,635,237,1457,637,
            635,1347,133,637,291,1251,133,641,619,1251,
            133,641,238,1479,273,1490,273,1501,273,1512,
            273,1523,273,1534,273,1545,273,1556,273,641,
            641
        };
    };
    public final static char baseAction[] = BaseAction.baseAction;
    public final int baseAction(int index) { return baseAction[index]; }
    public final static char lhs[] = baseAction;
    public final int lhs(int index) { return lhs[index]; };

    public interface TermCheck {
        public final static byte termCheck[] = {0,
            0,1,2,3,4,5,6,7,8,9,
            10,11,12,13,14,15,16,17,18,19,
            20,21,22,23,24,25,26,27,28,29,
            30,31,32,33,34,35,36,37,38,39,
            40,41,42,43,44,45,46,47,48,49,
            50,51,52,53,54,55,56,57,58,59,
            60,61,62,63,64,65,66,67,68,69,
            70,71,72,73,74,75,76,77,78,79,
            80,81,82,83,84,85,86,87,88,89,
            90,91,92,93,94,0,96,0,98,99,
            0,1,2,3,4,5,6,7,8,9,
            10,11,12,13,14,15,16,17,18,19,
            20,21,22,23,24,25,26,27,28,29,
            30,31,32,33,34,35,36,37,38,39,
            40,41,42,43,44,45,46,47,48,49,
            50,51,52,53,54,55,56,57,58,59,
            60,61,62,63,64,65,66,67,68,69,
            70,71,72,73,74,75,76,77,78,79,
            80,81,82,83,84,85,86,87,88,89,
            90,91,92,93,94,95,96,0,98,0,
            1,2,3,4,5,6,7,8,9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45,46,47,48,49,50,
            51,52,53,54,55,56,57,58,59,60,
            61,62,63,64,65,66,67,68,69,70,
            71,72,73,74,75,76,77,78,79,80,
            81,82,83,84,85,86,87,88,89,90,
            91,92,93,94,95,96,0,98,0,1,
            2,3,4,5,6,7,8,9,10,11,
            12,13,14,15,16,17,18,19,20,21,
            22,23,24,25,26,27,28,29,30,31,
            32,33,34,35,36,37,38,39,40,41,
            42,43,44,45,46,47,48,49,50,51,
            52,53,54,55,56,57,58,59,60,61,
            62,63,64,65,66,67,68,69,70,71,
            72,73,74,75,76,77,78,79,80,81,
            82,83,84,85,86,87,88,89,90,91,
            92,93,94,95,0,1,2,3,4,5,
            6,7,8,9,10,11,12,13,14,15,
            16,17,18,19,20,21,22,23,24,25,
            26,27,28,29,30,31,32,33,34,35,
            36,37,38,39,40,41,42,43,44,45,
            46,47,48,49,50,51,52,53,54,55,
            56,57,58,59,60,61,62,63,64,65,
            66,67,68,69,70,71,72,73,74,75,
            76,77,78,79,80,81,82,83,84,85,
            86,87,88,89,90,91,92,93,94,95,
            0,1,2,3,4,5,6,7,8,9,
            10,11,12,13,14,15,16,17,18,19,
            20,21,22,23,24,25,26,27,28,29,
            30,31,32,33,34,35,36,37,38,39,
            40,41,42,43,44,45,46,47,48,49,
            50,51,52,53,54,55,56,57,58,59,
            60,61,62,63,64,65,66,67,68,69,
            70,71,72,73,74,75,76,77,78,79,
            80,81,82,83,84,85,86,87,88,89,
            90,91,92,93,94,95,0,1,2,3,
            4,5,6,7,8,9,10,11,12,13,
            14,15,16,17,18,19,20,21,22,23,
            24,25,26,27,28,29,30,31,32,33,
            34,35,36,37,38,39,40,41,42,43,
            44,45,46,47,48,49,50,51,52,53,
            54,55,56,57,58,59,60,61,62,63,
            64,65,66,67,68,69,70,71,72,73,
            74,75,76,77,78,79,80,81,82,83,
            84,85,86,87,88,89,90,91,92,93,
            94,95,0,1,2,3,4,5,6,7,
            8,9,10,11,12,13,14,15,16,17,
            18,19,20,21,22,23,24,25,26,27,
            28,29,30,31,32,33,34,35,36,37,
            38,39,40,41,42,43,44,45,46,47,
            48,49,50,51,52,53,54,55,56,57,
            58,59,60,61,62,63,64,65,66,67,
            68,69,70,71,72,73,74,75,76,77,
            78,79,80,81,82,83,84,85,86,87,
            88,89,90,91,92,93,94,95,0,1,
            2,3,4,5,6,7,8,9,10,11,
            12,13,14,15,16,17,18,19,20,21,
            22,23,24,25,26,27,28,29,30,31,
            32,33,34,35,36,37,38,39,40,41,
            42,43,44,45,46,47,48,49,50,51,
            52,53,54,55,56,57,58,59,60,61,
            62,63,64,65,66,67,68,69,70,71,
            72,73,74,75,76,77,78,79,80,81,
            82,83,84,85,86,87,88,89,90,91,
            92,93,94,95,0,1,2,3,4,5,
            6,7,8,9,10,11,12,13,14,15,
            16,17,18,19,20,21,22,23,24,25,
            26,27,28,29,30,31,32,33,34,35,
            36,37,38,39,40,41,42,43,44,45,
            46,47,48,49,50,51,52,53,54,55,
            56,57,58,59,60,61,62,63,64,65,
            66,67,68,69,70,71,72,73,74,75,
            76,77,78,79,80,81,82,83,84,85,
            86,87,88,89,90,91,92,93,94,95,
            0,1,2,3,4,5,6,7,8,9,
            10,11,12,13,14,15,16,17,18,19,
            20,21,22,23,24,25,26,27,28,29,
            30,31,32,33,34,35,36,37,38,39,
            40,41,42,43,44,45,46,47,48,49,
            50,51,52,53,54,55,56,57,58,59,
            60,61,62,63,64,65,66,67,68,69,
            70,71,72,73,74,75,76,77,78,79,
            80,81,82,0,84,85,86,87,88,89,
            90,91,92,93,94,95,0,1,2,3,
            4,5,6,7,8,9,10,0,12,13,
            14,15,16,17,18,19,20,21,22,23,
            24,25,26,27,0,0,30,31,32,0,
            34,35,36,37,38,11,11,41,0,0,
            44,45,46,47,48,49,50,51,52,53,
            54,55,56,57,58,59,60,61,62,63,
            64,65,66,67,68,69,70,71,72,30,
            0,32,0,0,0,1,2,3,4,5,
            6,7,8,9,10,0,12,13,15,82,
            16,17,18,19,0,99,22,23,24,25,
            26,27,28,79,30,80,32,0,1,2,
            3,4,5,6,7,8,9,10,0,12,
            13,0,28,16,17,18,19,0,43,22,
            23,24,25,26,27,28,0,1,2,3,
            4,5,6,7,8,0,0,29,12,0,
            0,0,0,0,18,19,20,21,12,13,
            14,15,16,17,11,20,21,31,83,97,
            34,35,36,37,0,0,0,41,0,43,
            0,1,2,3,4,5,6,7,8,9,
            10,42,12,13,0,0,16,17,18,19,
            0,0,22,23,24,25,26,27,14,15,
            0,11,11,0,20,21,0,81,97,83,
            0,1,2,3,4,5,6,7,8,9,
            10,0,12,13,0,0,16,17,18,19,
            39,0,22,23,24,25,26,27,0,1,
            2,3,4,5,6,7,8,9,10,0,
            12,13,31,0,16,17,18,19,33,38,
            22,23,24,25,26,27,0,1,2,3,
            4,5,6,7,8,9,10,0,12,13,
            0,97,16,17,18,19,0,0,22,23,
            24,25,26,27,0,1,2,3,4,5,
            6,7,8,9,10,0,20,21,0,0,
            16,17,0,0,0,0,0,12,13,14,
            15,0,28,0,1,2,3,4,5,6,
            7,8,9,10,20,21,0,1,2,3,
            4,5,6,7,8,9,10,0,1,2,
            3,4,5,6,7,8,9,10,0,0,
            0,0,39,40,28,0,1,2,3,4,
            5,6,7,8,9,10,0,1,2,3,
            4,5,6,7,8,9,10,0,1,2,
            3,4,5,6,7,8,9,10,0,1,
            2,3,4,5,6,7,8,9,10,0,
            1,2,3,4,5,6,7,8,9,10,
            0,1,2,3,4,5,6,7,8,9,
            10,0,1,2,3,4,5,6,7,8,
            9,10,0,1,2,3,4,5,6,7,
            8,9,10,0,1,2,3,4,5,6,
            7,8,9,10,0,1,2,3,4,5,
            6,7,8,9,10,0,1,2,3,4,
            5,6,7,8,9,10,0,0,0,0,
            0,0,0,0,0,0,0,11,0,0,
            0,14,15,14,15,11,11,20,21,20,
            21,12,13,14,15,0,0,0,0,33,
            0,0,0,29,0,0,11,33,42,12,
            13,14,15,12,13,14,15,0,14,15,
            0,0,0,0,29,0,0,0,11,0,
            0,11,11,11,11,40,0,0,0,14,
            0,0,76,73,74,75,29,0,0,0,
            0,0,77,78,0,0,0,0,0,0,
            0,0,0,0,0,0,96,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0
        };
    };
    public final static byte termCheck[] = TermCheck.termCheck;
    public final int termCheck(int index) { return termCheck[index]; }

    public interface TermAction {
        public final static char termAction[] = {0,
            641,516,757,758,759,760,761,762,763,764,
            765,416,706,732,712,528,705,731,701,702,
            721,747,703,704,727,728,729,730,330,423,
            716,724,742,355,714,718,720,722,750,498,
            508,524,442,336,707,708,709,710,711,713,
            715,717,719,723,725,726,733,734,735,736,
            737,739,740,741,743,744,745,746,748,749,
            751,752,754,837,839,840,430,514,456,469,
            389,677,351,378,649,650,647,648,651,652,
            661,449,409,679,692,230,838,641,857,755,
            641,756,757,758,759,760,761,762,763,764,
            765,800,706,732,712,738,705,731,701,702,
            721,747,703,704,727,728,729,730,796,801,
            716,724,742,797,714,718,720,722,750,793,
            795,855,789,854,707,708,709,710,711,713,
            715,717,719,723,725,726,733,734,735,736,
            737,739,740,741,743,744,745,746,748,749,
            751,752,806,837,839,840,799,869,503,790,
            808,802,788,853,791,792,803,804,807,809,
            810,787,805,798,794,852,838,641,856,641,
            756,757,758,759,760,761,762,763,764,765,
            800,706,732,712,738,705,731,701,702,721,
            747,703,704,727,728,729,730,796,801,716,
            724,742,797,714,718,720,722,750,793,795,
            855,789,854,707,708,709,710,711,713,715,
            717,719,723,725,726,733,734,735,736,737,
            739,740,741,743,744,745,746,748,749,751,
            752,806,837,839,840,799,865,867,790,808,
            802,788,853,791,792,803,804,807,809,810,
            787,805,798,794,852,838,641,856,218,756,
            757,758,759,760,761,762,763,764,765,800,
            706,732,712,738,705,731,701,702,721,747,
            703,704,727,728,729,730,796,801,716,724,
            742,797,714,718,720,722,750,793,795,814,
            789,813,707,708,709,710,711,713,715,717,
            719,723,725,726,733,734,735,736,737,739,
            740,741,743,744,745,746,748,749,751,752,
            806,847,845,846,799,815,816,790,808,802,
            788,812,791,792,803,804,807,809,810,787,
            805,798,794,843,641,756,757,758,759,760,
            761,762,763,764,765,800,706,732,712,738,
            705,731,701,702,721,747,703,704,727,728,
            729,730,796,801,716,724,742,797,714,718,
            720,722,750,793,795,334,789,960,707,708,
            709,710,711,713,715,717,719,723,725,726,
            733,734,735,736,737,739,740,741,743,744,
            745,746,748,749,751,752,806,826,824,825,
            799,822,823,790,808,802,788,821,791,792,
            803,804,807,809,810,787,805,798,794,820,
            641,756,757,758,759,760,761,762,763,764,
            765,800,706,732,712,738,705,731,701,702,
            721,747,703,704,727,728,729,730,796,801,
            716,724,742,797,714,718,720,722,750,793,
            795,334,789,961,707,708,709,710,711,713,
            715,717,719,723,725,726,733,734,735,736,
            737,739,740,741,743,744,745,746,748,749,
            751,752,806,826,824,825,799,822,823,790,
            808,802,788,821,791,792,803,804,807,809,
            810,787,805,798,794,820,641,756,757,758,
            759,760,761,762,763,764,765,800,706,732,
            712,738,705,731,701,702,721,747,703,704,
            727,728,729,730,796,801,716,724,742,797,
            714,718,720,722,750,793,795,334,789,831,
            707,708,709,710,711,713,715,717,719,723,
            725,726,733,734,735,736,737,739,740,741,
            743,744,745,746,748,749,751,752,806,836,
            834,835,799,832,833,790,808,802,788,937,
            791,792,803,804,807,809,810,787,805,798,
            794,830,641,756,757,758,759,760,761,762,
            763,764,765,800,706,732,712,738,705,731,
            701,702,721,747,703,704,727,728,729,730,
            796,801,716,724,742,797,714,718,720,722,
            750,793,795,334,789,962,707,708,709,710,
            711,713,715,717,719,723,725,726,733,734,
            735,736,737,739,740,741,743,744,745,746,
            748,749,751,752,806,826,824,825,799,822,
            823,790,808,802,788,821,791,792,803,804,
            807,809,810,787,805,798,794,820,641,756,
            757,758,759,760,761,762,763,764,765,800,
            706,732,712,738,705,731,701,702,721,747,
            703,704,727,728,729,730,796,801,716,724,
            742,797,714,718,720,722,750,793,795,334,
            789,963,707,708,709,710,711,713,715,717,
            719,723,725,726,733,734,735,736,737,739,
            740,741,743,744,745,746,748,749,751,752,
            806,826,824,825,799,822,823,790,808,802,
            788,821,791,792,803,804,807,809,810,787,
            805,798,794,820,641,756,757,758,759,760,
            761,762,763,764,765,800,706,732,712,738,
            705,731,701,702,721,747,703,704,727,728,
            729,730,796,801,716,724,742,797,714,718,
            720,722,750,793,795,334,789,831,707,708,
            709,710,711,713,715,717,719,723,725,726,
            733,734,735,736,737,739,740,741,743,744,
            745,746,748,749,751,752,806,836,834,835,
            799,832,833,790,808,802,788,938,791,792,
            803,804,807,809,810,787,805,798,794,830,
            641,756,757,758,759,760,761,762,763,764,
            765,800,706,732,712,738,705,731,701,702,
            721,747,703,704,727,728,729,730,796,801,
            716,724,742,797,714,718,720,722,750,793,
            795,334,789,831,707,708,709,710,711,713,
            715,717,719,723,725,726,733,734,735,736,
            737,739,740,741,743,744,745,746,748,749,
            751,752,806,836,834,835,799,832,833,790,
            808,802,788,641,791,792,803,804,807,809,
            810,787,805,798,794,830,1,756,757,758,
            759,760,761,762,763,764,765,52,706,732,
            712,738,705,731,701,702,721,747,703,704,
            727,728,729,730,16,33,716,724,742,641,
            714,718,720,722,750,689,691,524,641,641,
            707,708,709,710,711,713,715,717,719,723,
            725,726,733,734,735,736,737,739,740,741,
            743,744,745,746,748,749,751,752,754,571,
            641,569,641,257,244,756,757,758,759,760,
            761,762,763,764,765,97,780,786,900,694,
            779,785,775,776,641,755,777,778,781,782,
            783,784,580,675,571,676,569,641,756,757,
            758,759,760,761,762,763,764,765,37,780,
            786,220,680,779,785,775,776,641,392,777,
            778,781,782,783,784,555,641,766,767,768,
            769,770,771,772,773,641,262,696,953,58,
            641,641,641,32,951,952,565,559,933,935,
            934,936,548,521,690,565,559,405,411,640,
            954,955,956,957,641,641,641,950,641,948,
            641,756,757,758,759,760,761,762,763,764,
            765,567,780,786,272,641,779,785,775,776,
            40,18,777,778,781,782,783,784,243,243,
            641,671,685,641,243,243,641,949,217,947,
            284,756,757,758,759,760,761,762,763,764,
            765,115,780,786,641,641,779,785,775,776,
            655,641,777,778,781,782,783,784,283,756,
            757,758,759,760,761,762,763,764,765,641,
            780,786,886,641,779,785,775,776,700,887,
            777,778,781,782,783,784,285,756,757,758,
            759,760,761,762,763,764,765,641,780,786,
            641,243,779,785,775,776,252,641,777,778,
            781,782,783,784,243,756,757,758,759,760,
            761,762,763,764,765,263,895,896,641,641,
            548,521,641,641,250,641,641,933,935,934,
            936,641,490,641,756,757,758,759,760,761,
            762,763,764,765,895,896,12,756,757,758,
            759,760,761,762,763,764,765,274,756,757,
            758,759,760,761,762,763,764,765,641,641,
            641,641,922,923,550,275,756,757,758,759,
            760,761,762,763,764,765,641,756,757,758,
            759,760,761,762,763,764,765,280,756,757,
            758,759,760,761,762,763,764,765,278,756,
            757,758,759,760,761,762,763,764,765,276,
            756,757,758,759,760,761,762,763,764,765,
            279,756,757,758,759,760,761,762,763,764,
            765,277,756,757,758,759,760,761,762,763,
            764,765,289,756,757,758,759,760,761,762,
            763,764,765,287,756,757,758,759,760,761,
            762,763,764,765,288,756,757,758,759,760,
            761,762,763,764,765,286,756,757,758,759,
            760,761,762,763,764,765,26,241,641,239,
            217,641,641,641,23,22,641,669,641,266,
            641,561,557,561,557,684,683,895,896,895,
            896,933,935,934,936,19,641,268,641,695,
            641,270,641,698,247,641,686,545,697,933,
            935,934,936,933,935,934,936,27,561,557,
            21,17,25,24,654,256,641,641,670,641,
            641,672,682,688,687,656,641,641,641,899,
            641,641,543,837,839,840,538,641,641,641,
            641,641,863,347,641,641,641,641,641,641,
            641,641,641,641,641,641,838
        };
    };
    public final static char termAction[] = TermAction.termAction;
    public final int termAction(int index) { return termAction[index]; }
    public final int asb(int index) { return 0; }
    public final int asr(int index) { return 0; }
    public final int nasb(int index) { return 0; }
    public final int nasr(int index) { return 0; }
    public final int terminalIndex(int index) { return 0; }
    public final int nonterminalIndex(int index) { return 0; }
    public final int scopePrefix(int index) { return 0;}
    public final int scopeSuffix(int index) { return 0;}
    public final int scopeLhs(int index) { return 0;}
    public final int scopeLa(int index) { return 0;}
    public final int scopeStateSet(int index) { return 0;}
    public final int scopeRhs(int index) { return 0;}
    public final int scopeState(int index) { return 0;}
    public final int inSymb(int index) { return 0;}
    public final String name(int index) { return null; }
    public final int getErrorSymbol() { return 0; }
    public final int getScopeUbound() { return 0; }
    public final int getScopeSize() { return 0; }
    public final int getMaxNameLength() { return 0; }

    public final static int
           NUM_STATES        = 88,
           NT_OFFSET         = 99,
           LA_STATE_OFFSET   = 967,
           MAX_LA            = 1,
           NUM_RULES         = 326,
           NUM_NONTERMINALS  = 52,
           NUM_SYMBOLS       = 151,
           SEGMENT_SIZE      = 8192,
           START_STATE       = 327,
           IDENTIFIER_SYMBOL = 0,
           EOFT_SYMBOL       = 97,
           EOLT_SYMBOL       = 65,
           ACCEPT_ACTION     = 640,
           ERROR_ACTION      = 641;

    public final static boolean BACKTRACK = false;

    public final int getNumStates() { return NUM_STATES; }
    public final int getNtOffset() { return NT_OFFSET; }
    public final int getLaStateOffset() { return LA_STATE_OFFSET; }
    public final int getMaxLa() { return MAX_LA; }
    public final int getNumRules() { return NUM_RULES; }
    public final int getNumNonterminals() { return NUM_NONTERMINALS; }
    public final int getNumSymbols() { return NUM_SYMBOLS; }
    public final int getSegmentSize() { return SEGMENT_SIZE; }
    public final int getStartState() { return START_STATE; }
    public final int getStartSymbol() { return lhs[0]; }
    public final int getIdentifierSymbol() { return IDENTIFIER_SYMBOL; }
    public final int getEoftSymbol() { return EOFT_SYMBOL; }
    public final int getEoltSymbol() { return EOLT_SYMBOL; }
    public final int getAcceptAction() { return ACCEPT_ACTION; }
    public final int getErrorAction() { return ERROR_ACTION; }
    public final boolean isValidForParser() { return isValidForParser; }
    public final boolean getBacktrack() { return BACKTRACK; }

    public final int originalState(int state) { return 0; }
    public final int asi(int state) { return 0; }
    public final int nasi(int state) { return 0; }
    public final int inSymbol(int state) { return 0; }

    public final int ntAction(int state, int sym) {
        return baseAction[state + sym];
    }

    public final int tAction(int state, int sym) {
        int i = baseAction[state],
            k = i + sym;
        return termAction[termCheck[k] == sym ? k : i];
    }
    public final int lookAhead(int la_state, int sym) {
        int k = la_state + sym;
        return termAction[termCheck[k] == sym ? k : la_state];
    }
}
