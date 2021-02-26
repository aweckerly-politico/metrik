import React, { FC, useEffect, useRef, useState } from "react";
import {
	CartesianGrid,
	Line,
	LineChart as RechartsLineChart,
	ResponsiveContainer,
	XAxis,
	YAxis,
} from "recharts";
import { Metrics } from "../clients/apis";
import { GRAY_1, GRAY_7 } from "../constants/styles";
import { css } from "@emotion/react";
import { throttle } from "lodash";
import { AxisDomain } from "recharts/types/util/types";

export interface CustomizeTickProps {
	x?: number;
	y?: number;
	textAnchor?: string;
	index?: number;
	data: Metrics[];
	payload?: any;
}

interface LineChartProps {
	data: Metrics[];
	yaxisFormatter: (value: string) => string;
	unit: string;
	CustomizeTick: FC<CustomizeTickProps>;
	yAxisDomain?: AxisDomain;
}

const lineUnit = 100;
const yAxisWidth = 72;
const minLengthForDisplayScrollBar = 10;

const chartContainerStyle = css({
	position: "relative",
	height: 300,
	"overflow-x": "auto",
});

const yAxisStyles = css({
	width: yAxisWidth,
	height: 300,
	position: "absolute",
	backgroundColor: "#ffffff",
	zIndex: 1000,
});

export const LineChart: FC<LineChartProps> = ({
	data,
	yaxisFormatter,
	unit,
	CustomizeTick,
	yAxisDomain,
}) => {
	const ref = useRef<HTMLDivElement>(null);
	const scrollWidth = data.length ? lineUnit * (data.length - 1) + yAxisWidth : 0;
	const [xAxisInterval, setXAxisInterval] = useState<"preserveEnd" | 0>(0);

	useEffect(() => {
		const adjustXAxisInterval = throttle(() => {
			setXAxisInterval(
				data.length < minLengthForDisplayScrollBar &&
					(ref.current?.offsetWidth === undefined || ref.current?.offsetWidth / data.length <= 80)
					? "preserveEnd"
					: 0
			);
		}, 500);

		adjustXAxisInterval();
		window.addEventListener("resize", adjustXAxisInterval);

		return () => {
			window.removeEventListener("resize", adjustXAxisInterval);
		};
	}, []);

	return (
		<div ref={ref}>
			<div css={yAxisStyles}>
				<ResponsiveContainer width="100%" height="80%" id={"levelSvg"}>
					<RechartsLineChart
						margin={{
							top: 20,
							right: 30,
							left: 20,
							bottom: 20,
						}}>
						<YAxis
							tickFormatter={yaxisFormatter}
							axisLine={false}
							label={{ value: unit, angle: -90, position: "insideLeft" }}
							tickLine={false}
							domain={yAxisDomain}
						/>
					</RechartsLineChart>
				</ResponsiveContainer>
			</div>

			<div css={chartContainerStyle}>
				<ResponsiveContainer
					id={"chartSvg"}
					width={data.length >= minLengthForDisplayScrollBar ? scrollWidth : "100%"}>
					<RechartsLineChart
						data={data}
						margin={{
							top: 20,
							right: 30,
							left: 20,
							bottom: 20,
						}}>
						<CartesianGrid
							stroke="#416180"
							strokeWidth={0.5}
							strokeOpacity={0.2}
							vertical={false}
						/>
						<XAxis
							interval={xAxisInterval}
							dataKey="startTimestamp"
							stroke="#416180"
							strokeWidth={0.5}
							strokeOpacity={0.45}
							height={50}
							padding={{ left: 30, right: 30 }}
							tick={<CustomizeTick data={data} />}
						/>
						<YAxis
							tickFormatter={yaxisFormatter}
							axisLine={false}
							label={{ value: unit, angle: -90, position: "insideLeft" }}
							tickLine={false}
							domain={yAxisDomain}
						/>
						<Line
							type="monotone"
							dataKey="value"
							stroke={GRAY_7}
							strokeWidth={2}
							fill={GRAY_1}
							isAnimationActive={false}
							// eslint-disable-next-line @typescript-eslint/ban-ts-comment
							// @ts-ignore
							label={{
								position: "top",
								formatter: yaxisFormatter,
								fontSize: 12,
								style: { transform: "translateY(-5px)" },
							}}
						/>
					</RechartsLineChart>
				</ResponsiveContainer>
			</div>
		</div>
	);
};
