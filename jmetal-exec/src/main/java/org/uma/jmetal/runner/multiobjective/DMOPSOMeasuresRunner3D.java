package org.uma.jmetal.runner.multiobjective;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.uma.jmetal.algorithm.multiobjective.dmopso.DMOPSO.FunctionType;
import org.uma.jmetal.algorithm.multiobjective.dmopso.DMOPSOMeasures;
import org.uma.jmetal.measure.MeasureListener;
import org.uma.jmetal.measure.MeasureManager;
import org.uma.jmetal.measure.impl.BasicMeasure;
import org.uma.jmetal.measure.impl.CountingMeasure;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.runner.AbstractAlgorithmRunner;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.chartcontainer.ChartContainer;
import org.uma.jmetal.util.front.imp.ArrayFront;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for configuring and running the DMOPSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class DMOPSOMeasuresRunner3D extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   * @throws SecurityException Invoking command: java org.uma.jmetal.runner.multiobjective.DMOPSORunner
   *                           problemName [referenceFront]
   */
  public static void main(String[] args) throws Exception {
    DoubleProblem problem;
    DMOPSOMeasures algorithm;

    String referenceParetoFront = "";

    String problemName;
    if (args.length == 1) {
      problemName = args[0];
    } else if (args.length == 2) {
      problemName = args[0];
      referenceParetoFront = args[1];
    } else {
      problemName = "org.uma.jmetal.problem.multiobjective.dtlz.DTLZ2";
      referenceParetoFront = "jmetal-problem/src/test/resources/pareto_fronts/DTLZ2.3D.pf";
    }

    problem = (DoubleProblem) ProblemUtils.<DoubleSolution>loadProblem(problemName);

    algorithm = new DMOPSOMeasures(problem, 100, 150, 0.0, 0.1, 0.0, 1.0, 1.5, 2.5, 1.5, 2.5, 0.1, 0.4, -1.0, -1.0,
            FunctionType.TCHE, "MOEAD_Weights", 2);

    algorithm.setReferenceFront(new ArrayFront(referenceParetoFront));

        /* Measure management */
    MeasureManager measureManager = ((DMOPSOMeasures) algorithm).getMeasureManager();

    BasicMeasure<List<DoubleSolution>> solutionListMeasure = (BasicMeasure<List<DoubleSolution>>) measureManager
            .<List<DoubleSolution>>getPushMeasure("currentPopulation");
    CountingMeasure iterationMeasure = (CountingMeasure) measureManager.<Long>getPushMeasure("currentEvaluation");
    BasicMeasure<Double> hypervolumeMeasure = (BasicMeasure<Double>) measureManager
            .<Double>getPushMeasure("hypervolume");
    BasicMeasure<Double> epsilonMeasure = (BasicMeasure<Double>) measureManager.<Double>getPushMeasure("epsilon");

    List<Double> referencePoint = new ArrayList<>() ;
		referencePoint.add(1.0);
		referencePoint.add(0.5);
		referencePoint.add(0.25);
    
    ChartContainer chart = new ChartContainer(algorithm.getName(), 200);
		//chart.setFrontChart(0, 1, referenceParetoFront);
		chart.addFrontChart(0, 1);
		chart.addFrontChart(0, 2);
		chart.addFrontChart(1, 2);
    //chart.setVarChart(0, 1);
		chart.setReferencePoint(referencePoint);
    chart.initChart();

    solutionListMeasure.register(new ChartListener(chart));
    iterationMeasure.register(new IterationListener(chart));

        /* End of measure management */

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
    chart.saveChart("./chart", BitmapFormat.PNG);

    List<DoubleSolution> population = algorithm.getResult();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    if (!referenceParetoFront.equals("")) {
      printQualityIndicators(population, referenceParetoFront);
    }

  }

  private static class ChartListener implements MeasureListener<List<DoubleSolution>> {
    private ChartContainer chart;
    private int iteration = 0;

    public ChartListener(ChartContainer chart) {
      this.chart = chart;
			for (XYChart xychart : chart.getCharts()) {
				xychart.setTitle("Iteration: " + this.iteration);
			}
      //this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
    }

    private void refreshChart(List<DoubleSolution> solutionList) {
      if (this.chart != null) {
        iteration++;
				for (XYChart xychart : chart.getCharts()) {
					xychart.setTitle("Iteration: " + this.iteration);
				}
				
        //this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
        this.chart.updateFrontCharts(solutionList);
        this.chart.refreshCharts();
      }
    }

    @Override
    synchronized public void measureGenerated(List<DoubleSolution> solutions) {
      refreshChart(solutions);
    }
  }

  private static class IterationListener implements MeasureListener<Long> {
    ChartContainer chart;

    public IterationListener(ChartContainer chart) {
      this.chart = chart;
			for (XYChart xychart : chart.getCharts()) {
				xychart.setTitle("Iteration: " + 0);
			}
      //this.chart.getFrontChart().setTitle("Iteration: " + 0);
    }

    @Override
    synchronized public void measureGenerated(Long iteration) {
      if (this.chart != null) {
				for (XYChart xychart : chart.getCharts()) {
					xychart.setTitle("Iteration: " + iteration);
				}
        //this.chart.getFrontChart().setTitle("Iteration: " + iteration);
      }
    }
  }
}