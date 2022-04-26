package constants;

public interface PhysicalConstants {

	// Физические константы в СИ
	double k = 9E9;
	double kB = 1.3807E-23;
	double g = 9.81;
	double G = 67E-12;
	double me = 9.11E-31;
	double e = 1.6E-19;

	// Коэфициенты перехода в СИ
	// Длина, м
	double au = 150E9;
	double km = 1E3;
	double m = 1d;
	double dm = 1E-1;
	double cm = 1E-2;
	double mm = 1E-3;
	double um = 1E-6;
	double nm = 1E-9;
	double ang = 1E-10;
	double in = 0.0254;
	double ft = 0.3048;
	// Скорость, m/c
	double mPerS = 1d;
	double cmPerS = 1E-2;
	double kmPerH = 1 / 3.6;
	double c = 3E8;
	// Ускорение, м/с^2
	double gn = g;
	// Время, c
	double fs = 1E-15;
	double ps = 1E-12;
	double ns = 1E-9;
	double us = 1E-6;
	double ms = 1E-3;
	double s = 1d;
	double min = 60;
	double h = 3600;
	double day = 86400;
	// Площадь, m^2
	double m2 = 1d;
	double cm2 = 1E-4;
	double mm2 = 1E-6;
	double ft2 = ft * ft;
	double inch2 = 6.4516 * cm2;
	// Масса
	double kg = 1d;
	double gr = 1E-3;
	double lb = 0.45359237;
	double t = 1E3;
	double aem = 1.66E-27;
	// Частота
	double hz = 1d;
	double kHz = 1E3;
	double MHz = 1E6;
	// Сила
	double H = 1d;
	double kH = 1E3;
	double MH = 1E6;
	double kgf = kg * gn;
	double tf = t * gn;
	double lbf = lb * gn;
	// Энергия
	double dj = 1d;
	double ev = 1.6E-19;
	// Давление
	double Pa = 1d;
	double psi = 6894.76;

}