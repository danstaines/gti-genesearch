package org.ensembl.genesearch.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

import htsjdk.samtools.util.BufferedLineReader;

public class VcfUtilsTest {

	@Test
	public void testSpec() throws IOException {
		BufferedLineReader vcfR = new BufferedLineReader(this.getClass().getResourceAsStream("/spec_sample.vcf"));
		Optional<String> colsLine = vcfR.lines().filter(VcfUtils.isColsLine()).findFirst();
		assertTrue("Cols line found", colsLine.isPresent());
		List<Map<String, Object>> variants = vcfR.lines().map(VcfUtils::vcfLineToMap).collect(Collectors.toList());
		assertEquals("Variants found", 5, variants.size());		
		Optional<Map<String, Object>> oSnp = variants.stream().filter(v -> "rs6040355".equals(v.get("ID"))).findFirst();
		assertTrue("Variant found", oSnp.isPresent());
		//20	1110696	rs6040355	A	G,T	67	PASS	NS=2;DP=10;AF=0.333,0.667;AA=T;DB	GT:GQ:DP:HQ	1|2:21:6:23,27	2|1:2:0:18,2	2/2:35:4:1,1
		Map<String, Object> snp = oSnp.get();
		assertEquals("ID", "rs6040355", snp.get("ID"));
		assertEquals("CHROM", "20", snp.get("CHROM"));
		assertEquals("POS", "1110696", snp.get("POS"));
		assertEquals("REF", "A", snp.get("REF"));
		assertEquals("ALT", "G,T", snp.get("ALT"));
		assertEquals("QUAL", "67", snp.get("QUAL"));
		assertEquals("FILTER", "PASS", snp.get("FILTER"));
		assertEquals("NS", "2", snp.get("NS"));
		assertEquals("DP", "10", snp.get("DP"));
		assertEquals("AF", "0.333,0.667", snp.get("AF"));
		assertEquals("AA", "T", snp.get("AA"));
		assertEquals("DB", true, snp.get("DB"));
		assertFalse("No genotypes", snp.containsKey("genotypes"));
	}

	@Test
	public void testSpecGenotype() throws IOException {
		BufferedLineReader vcfR = new BufferedLineReader(this.getClass().getResourceAsStream("/spec_sample.vcf"));
		Optional<String> colsLine = vcfR.lines().filter(VcfUtils.isColsLine()).findFirst();
		assertTrue("Cols line found", colsLine.isPresent());
		String[] genotypes = VcfUtils.getGenotypes(colsLine.get());
		assertEquals("Genotypes found", 3, genotypes.length);
		List<Map<String, Object>> variants = vcfR.lines().map(l -> VcfUtils.vcfLineToMap(l, genotypes)).collect(Collectors.toList());
		assertEquals("Variants found", 5, variants.size());		
		Optional<Map<String, Object>> oSnp = variants.stream().filter(v -> "rs6040355".equals(v.get("ID"))).findFirst();
		assertTrue("Variant found", oSnp.isPresent());
		Map<String, Object> snp = oSnp.get();
		assertEquals("ID", "rs6040355", snp.get("ID"));
		assertEquals("CHROM", "20", snp.get("CHROM"));
		assertEquals("POS", "1110696", snp.get("POS"));
		assertEquals("REF", "A", snp.get("REF"));
		assertEquals("ALT", "G,T", snp.get("ALT"));
		assertEquals("QUAL", "67", snp.get("QUAL"));
		assertEquals("FILTER", "PASS", snp.get("FILTER"));
		assertEquals("NS", "2", snp.get("NS"));
		assertEquals("DP", "10", snp.get("DP"));
		assertEquals("AF", "0.333,0.667", snp.get("AF"));
		assertEquals("AA", "T", snp.get("AA"));
		assertEquals("DB", true, snp.get("DB"));
		assertTrue("Genotypes", snp.containsKey("genotypes"));
		List<Map<String,Object>> snpGenotypes = (List<Map<String, Object>>) snp.get("genotypes");
		assertEquals("Genotypes found", 3, snpGenotypes.size());
		Map<String,Object> genotype = snpGenotypes.get(0);
		assertEquals("Genotype attributes", 5, genotype.keySet().size());
		assertEquals("GENOTYPE_ID", "NA00001", genotype.get("GENOTYPE_ID"));
		// 1|2:21:6:23,27
		assertEquals("GT", "1|2", genotype.get("GT"));
		assertEquals("GQ", "21", genotype.get("GQ"));
		assertEquals("DP", "6", genotype.get("DP"));
		assertEquals("HQ", "23,27", genotype.get("HQ"));
	}
	
}
