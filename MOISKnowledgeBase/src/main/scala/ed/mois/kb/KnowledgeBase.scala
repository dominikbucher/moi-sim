/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb

import ed.mois.kb.model._
import ed.mois.kb.model.auth._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import org.squeryl.KeyedEntity

object KnowledgeBase extends Schema {

  val userprofile = table[Public_UserProfile]

  val evidence = table[Public_Evidence]

  val entrydata = table[Public_EntryData]

  val evidencedentrydata = table[Public_EvidencedEntryData]

  val entrybooleandata = table[Public_EntryBooleanData]

  val entrychardata = table[Public_EntryCharData]

  val entryfloatdata = table[Public_EntryFloatData]

  val entrypositivefloatdata = table[Public_EntryPositiveFloatData]

  val entrytextdata = table[Public_EntryTextData]

  val bindingsite = table[Public_BindingSite]

  val biomasscomposition = table[Public_BiomassComposition]

  val codon = table[Public_Codon]

  val coenzymeparticipant = table[Public_CoenzymeParticipant]

  val crossreference = table[Public_CrossReference]

  val disulfidebond = table[Public_DisulfideBond]

  val dnafootprint = table[Public_DNAFootprint]

  val enzymeparticipant = table[Public_EnzymeParticipant]

  val homolog = table[Public_Homolog]

  val kinetics = table[Public_Kinetics]

  val mediacomposition = table[Public_MediaComposition]

  val metabolitemapcoordinate = table[Public_MetaboliteMapCoordinate]

  val modificationreaction = table[Public_ModificationReaction]

  val prostheticgroupparticipant = table[Public_ProstheticGroupParticipant]

  val proteincomplexbiosythesisparticipant = table[Public_ProteinComplexBiosythesisParticipant]

  val reactionmapcoordinate = table[Public_ReactionMapCoordinate]

  val reactionstoichiometryparticipant = table[Public_ReactionStoichiometryParticipant]

  val signalsequence = table[Public_SignalSequence]

  val synonym = table[Public_Synonym]

  val entry = table[Public_Entry]

  implicit object speciescomponentKED extends KeyedEntityDef[Public_SpeciesComponent, Int] {
    def getId(a: Public_SpeciesComponent) = a.parent_ptr_entry_id.get
    def isPersisted(a: Public_SpeciesComponent) = a.parent_ptr_entry_id.get > 0
    def idPropertyName = "parent_ptr_entry_id"
  }
  val speciescomponent = table[Public_SpeciesComponent]

  implicit object moleculeKED extends KeyedEntityDef[Public_Molecule, Int] {
    def getId(a: Public_Molecule) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Molecule) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val molecule = table[Public_Molecule]

  implicit object proteinKED extends KeyedEntityDef[Public_Protein, Int] {
    def getId(a: Public_Protein) = a.parent_ptr_molecule_id.get
    def isPersisted(a: Public_Protein) = a.parent_ptr_molecule_id.get > 0
    def idPropertyName = "parent_ptr_molecule_id"
  }
  val protein = table[Public_Protein]

  implicit object chromosomeKED extends KeyedEntityDef[Public_Chromosome, Int] {
    def getId(a: Public_Chromosome) = a.parent_ptr_molecule_id.get
    def isPersisted(a: Public_Chromosome) = a.parent_ptr_molecule_id.get > 0
    def idPropertyName = "parent_ptr_molecule_id"
  }
  val chromosome = table[Public_Chromosome]

  implicit object chromosomefeatureKED extends KeyedEntityDef[Public_ChromosomeFeature, Int] {
    def getId(a: Public_ChromosomeFeature) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_ChromosomeFeature) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val chromosomefeature = table[Public_ChromosomeFeature]

  implicit object compartmentKED extends KeyedEntityDef[Public_Compartment, Int] {
    def getId(a: Public_Compartment) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Compartment) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val compartment = table[Public_Compartment]

  implicit object geneKED extends KeyedEntityDef[Public_Gene, Int] {
    def getId(a: Public_Gene) = a.parent_ptr_molecule_id.get
    def isPersisted(a: Public_Gene) = a.parent_ptr_molecule_id.get > 0
    def idPropertyName = "parent_ptr_molecule_id"
  }
  val gene = table[Public_Gene]

  implicit object metaboliteKED extends KeyedEntityDef[Public_Metabolite, Int] {
    def getId(a: Public_Metabolite) = a.parent_ptr_molecule_id.get
    def isPersisted(a: Public_Metabolite) = a.parent_ptr_molecule_id.get > 0
    def idPropertyName = "parent_ptr_molecule_id"
  }
  val metabolite = table[Public_Metabolite]

  implicit object noteKED extends KeyedEntityDef[Public_Note, Int] {
    def getId(a: Public_Note) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Note) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val note = table[Public_Note]

  implicit object parameterKED extends KeyedEntityDef[Public_Parameter, Int] {
    def getId(a: Public_Parameter) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Parameter) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val parameter = table[Public_Parameter]

  implicit object pathwayKED extends KeyedEntityDef[Public_Pathway, Int] {
    def getId(a: Public_Pathway) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Pathway) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val pathway = table[Public_Pathway]

  implicit object processKED extends KeyedEntityDef[Public_Process, Int] {
    def getId(a: Public_Process) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Process) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val process = table[Public_Process]

  implicit object proteincomplexKED extends KeyedEntityDef[Public_ProteinComplex, Int] {
    def getId(a: Public_ProteinComplex) = a.parent_ptr_protein_id.get
    def isPersisted(a: Public_ProteinComplex) = a.parent_ptr_protein_id.get > 0
    def idPropertyName = "parent_ptr_protein_id"
  }
  val proteincomplex = table[Public_ProteinComplex]

  implicit object proteinmonomerKED extends KeyedEntityDef[Public_ProteinMonomer, Int] {
    def getId(a: Public_ProteinMonomer) = a.parent_ptr_protein_id.get
    def isPersisted(a: Public_ProteinMonomer) = a.parent_ptr_protein_id.get > 0
    def idPropertyName = "parent_ptr_protein_id"
  }
  val proteinmonomer = table[Public_ProteinMonomer]

  implicit object reactionKED extends KeyedEntityDef[Public_Reaction, Int] {
    def getId(a: Public_Reaction) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Reaction) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val reaction = table[Public_Reaction]

  implicit object referenceKED extends KeyedEntityDef[Public_Reference, Int] {
    def getId(a: Public_Reference) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Reference) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val reference = table[Public_Reference]

  implicit object speciesKED extends KeyedEntityDef[Public_Species, Int] {
    def getId(a: Public_Species) = a.parent_ptr_entry_id.get
    def isPersisted(a: Public_Species) = a.parent_ptr_entry_id.get > 0
    def idPropertyName = "parent_ptr_entry_id"
  }
  val species = table[Public_Species]

  implicit object stateKED extends KeyedEntityDef[Public_State, Int] {
    def getId(a: Public_State) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_State) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val state = table[Public_State]

  implicit object stimulusKED extends KeyedEntityDef[Public_Stimulus, Int] {
    def getId(a: Public_Stimulus) = a.parent_ptr_molecule_id.get
    def isPersisted(a: Public_Stimulus) = a.parent_ptr_molecule_id.get > 0
    def idPropertyName = "parent_ptr_molecule_id"
  }
  val stimulus = table[Public_Stimulus]

  implicit object transcriptionunitKED extends KeyedEntityDef[Public_TranscriptionUnit, Int] {
    def getId(a: Public_TranscriptionUnit) = a.parent_ptr_molecule_id.get
    def isPersisted(a: Public_TranscriptionUnit) = a.parent_ptr_molecule_id.get > 0
    def idPropertyName = "parent_ptr_molecule_id"
  }
  val transcriptionunit = table[Public_TranscriptionUnit]

  implicit object transcriptionalregulationKED extends KeyedEntityDef[Public_TranscriptionalRegulation, Int] {
    def getId(a: Public_TranscriptionalRegulation) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_TranscriptionalRegulation) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val transcriptionalregulation = table[Public_TranscriptionalRegulation]

  implicit object typeKED extends KeyedEntityDef[Public_Type, Int] {
    def getId(a: Public_Type) = a.parent_ptr_species_component_id.get
    def isPersisted(a: Public_Type) = a.parent_ptr_species_component_id.get > 0
    def idPropertyName = "parent_ptr_species_component_id"
  }
  val ttype = table[Public_Type]


  // Class Evidence --------------------
  val evidence_references = manyToManyRelation(evidence, reference)
    .via[Public_Evidence_references]((e, s, es) => (es.evidence_id === e.id, s.parent_ptr_species_component_id === es.reference_id))

  // Class EvidencedEntryData --------------------
  val evidencedentrydata_evidence = manyToManyRelation(evidencedentrydata, evidence)
    .via[Public_EvidencedEntryData_evidence]((e, s, es) => (es.evidencedentrydata_id === e.id, s.id === es.evidence_id))

  // Class EntryBooleanData --------------------
  val entrybooleandata_evidence = manyToManyRelation(entrybooleandata, evidence)
    .via[Public_EntryBooleanData_Evidence]((e, s, es) => (es.entrybooleandata_id === e.id, s.id === es.evidence_id))

  // Class EntryCharData --------------------
  val entrychardata_evidence = manyToManyRelation(entrychardata, evidence)
    .via[Public_EntryCharData_Evidence]((e, s, es) => (es.entrychardata_id === e.id, s.id === es.evidence_id))

  // Class EntryFloatData --------------------
  val entryfloatdata_evidence = manyToManyRelation(entryfloatdata, evidence)
    .via[Public_EntryFloatData_Evidence]((e, s, es) => (es.entryfloatdata_id === e.id, s.id === es.evidence_id))

  // Class EntryPositiveFloatData --------------------
  val entrypositivefloatdata_evidence = manyToManyRelation(entrypositivefloatdata, evidence)
    .via[Public_EntryPositiveFloatData_Evidence]((e, s, es) => (es.entrypositivefloatdata_id === e.id, s.id === es.evidence_id))

  // Class EntryTextData --------------------
  val entrytextdata_evidence = manyToManyRelation(entrytextdata, evidence)
    .via[Public_EntryTextData_Evidence]((e, s, es) => (es.entrytextdata_id === e.id, s.id === es.evidence_id))

  // Class BindingSite --------------------
  val bindingsite_evidence = manyToManyRelation(bindingsite, evidence)
    .via[Public_BindingSite_Evidence]((e, s, es) => (es.bindingsite_id === e.id, s.id === es.evidence_id))

  // Class BiomassComposition --------------------
  val biomasscomposition_evidence = manyToManyRelation(biomasscomposition, evidence)
    .via[Public_BiomassComposition_Evidence]((e, s, es) => (es.biomasscomposition_id === e.id, s.id === es.evidence_id))

  // Class Codon --------------------
  val codon_evidence = manyToManyRelation(codon, evidence)
    .via[Public_Codon_Evidence]((e, s, es) => (es.codon_id === e.id, s.id === es.evidence_id))

  // Class CoenzymeParticipant --------------------
  val coenzymeparticipant_evidence = manyToManyRelation(coenzymeparticipant, evidence)
    .via[Public_CoenzymeParticipant_Evidence]((e, s, es) => (es.coenzymeparticipant_id === e.id, s.id === es.evidence_id))

  // Class DisulfideBond --------------------
  val disulfidebond_evidence = manyToManyRelation(disulfidebond, evidence)
    .via[Public_DisulfideBond_Evidence]((e, s, es) => (es.disulfidebond_id === e.id, s.id === es.evidence_id))

  // Class DNAFootprint --------------------
  val dnafootprint_evidence = manyToManyRelation(dnafootprint, evidence)
    .via[Public_DNAFootprint_Evidence]((e, s, es) => (es.dnafootprint_id === e.id, s.id === es.evidence_id))

  // Class EnzymeParticipant --------------------
  val enzymeparticipant_evidence = manyToManyRelation(enzymeparticipant, evidence)
    .via[Public_EnzymeParticipant_Evidence]((e, s, es) => (es.enzymeparticipant_id === e.id, s.id === es.evidence_id))

  // Class Homolog --------------------
  val homolog_evidence = manyToManyRelation(homolog, evidence)
    .via[Public_Homolog_Evidence]((e, s, es) => (es.homolog_id === e.id, s.id === es.evidence_id))

  // Class Kinetics --------------------
  val kinetics_evidence = manyToManyRelation(kinetics, evidence)
    .via[Public_Kinetics_Evidence]((e, s, es) => (es.kinetics_id === e.id, s.id === es.evidence_id))

  // Class MediaComposition --------------------
  val mediacomposition_evidence = manyToManyRelation(mediacomposition, evidence)
    .via[Public_MediaComposition_Evidence]((e, s, es) => (es.mediacomposition_id === e.id, s.id === es.evidence_id))

  // Class ModificationReaction --------------------
  val modificationreaction_evidence = manyToManyRelation(modificationreaction, evidence)
    .via[Public_ModificationReaction_Evidence]((e, s, es) => (es.modificationreaction_id === e.id, s.id === es.evidence_id))

  // Class ProstheticGroupParticipant --------------------
  val prostheticgroupparticipant_evidence = manyToManyRelation(prostheticgroupparticipant, evidence)
    .via[Public_ProstheticGroupParticipant_Evidence]((e, s, es) => (es.prostheticgroupparticipant_id === e.id, s.id === es.evidence_id))

  // Class ProteinComplexBiosythesisParticipant --------------------
  val proteincomplexbiosythesisparticipant_evidence = manyToManyRelation(proteincomplexbiosythesisparticipant, evidence)
    .via[Public_ProteinComplexBiosythesisParticipant_Evidence]((e, s, es) => (es.proteincomplexbiosythesisparticipant_id === e.id, s.id === es.evidence_id))

  // Class ReactionStoichiometryParticipant --------------------
  val reactionstoichiometryparticipant_evidence = manyToManyRelation(reactionstoichiometryparticipant, evidence)
    .via[Public_ReactionStoichiometryParticipant_Evidence]((e, s, es) => (es.reactionstoichiometryparticipant_id === e.id, s.id === es.evidence_id))

  // Class SignalSequence --------------------
  val signalsequence_evidence = manyToManyRelation(signalsequence, evidence)
    .via[Public_SignalSequence_Evidence]((e, s, es) => (es.signalsequence_id === e.id, s.id === es.evidence_id))

  // Class Entry --------------------
  val entry_cross_references = manyToManyRelation(entry, crossreference)
    .via[Public_Entry_cross_references]((e, s, es) => (es.entry_id === e.id, s.id === es.crossreference_id))
  val entry_synonyms = manyToManyRelation(entry, synonym)
    .via[Public_Entry_synonyms]((e, s, es) => (es.entry_id === e.id, s.id === es.synonym_id))

  // Class SpeciesComponent --------------------
  val speciescomponent_references = manyToManyRelation(speciescomponent, reference)
    .via[Public_SpeciesComponent_references]((e, s, es) => (es.speciescomponent_id === e.parent_ptr_entry_id, s.parent_ptr_species_component_id === es.reference_id))
  val speciescomponent_type = manyToManyRelation(speciescomponent, ttype)
    .via[Public_SpeciesComponent_type]((e, s, es) => (es.speciescomponent_id === e.parent_ptr_entry_id, s.parent_ptr_species_component_id === es.type_id))

  // Class Protein --------------------
  val protein_chaperones = manyToManyRelation(protein, protein)
    .via[Public_Protein_chaperones]((e, s, es) => (es.from_protein_id === e.parent_ptr_molecule_id, s.parent_ptr_molecule_id === es.to_protein_id))
  val protein_prosthetic_groups = manyToManyRelation(protein, prostheticgroupparticipant)
    .via[Public_Protein_prosthetic_groups]((e, s, es) => (es.protein_id === e.parent_ptr_molecule_id, s.id === es.prostheticgroupparticipant_id))

  // Class Gene --------------------
  val gene_homologs = manyToManyRelation(gene, homolog)
    .via[Public_Gene_homologs]((e, s, es) => (es.gene_id === e.parent_ptr_molecule_id, s.id === es.homolog_id))
  val gene_codons = manyToManyRelation(gene, codon)
    .via[Public_Gene_codons]((e, s, es) => (es.gene_id === e.parent_ptr_molecule_id, s.id === es.codon_id))

  // Class Metabolite --------------------
  val metabolite_map_coordinates = manyToManyRelation(metabolite, metabolitemapcoordinate)
    .via[Public_Metabolite_map_coordinates]((e, s, es) => (es.metabolite_id === e.parent_ptr_molecule_id, s.id === es.metabolitemapcoordinate_id))
  val metabolite_biomass_composition = manyToManyRelation(metabolite, biomasscomposition)
    .via[Public_Metabolite_biomass_composition]((e, s, es) => (es.metabolite_id === e.parent_ptr_molecule_id, s.id === es.biomasscomposition_id))

  // Class Parameter --------------------
  val parameter_molecules = manyToManyRelation(parameter, molecule)
    .via[Public_Parameter_molecules]((e, s, es) => (es.parameter_id === e.parent_ptr_species_component_id, s.parent_ptr_species_component_id === es.molecule_id))
  val parameter_reactions = manyToManyRelation(parameter, reaction)
    .via[Public_Parameter_reactions]((e, s, es) => (es.parameter_id === e.parent_ptr_species_component_id, s.parent_ptr_species_component_id === es.reaction_id))

  // Class ProteinComplex --------------------
  val proteincomplex_disulfide_bonds = manyToManyRelation(proteincomplex, disulfidebond)
    .via[Public_ProteinComplex_disulfide_bonds]((e, s, es) => (es.proteincomplex_id === e.parent_ptr_protein_id, s.id === es.disulfidebond_id))
  val proteincomplex_biosynthesis = manyToManyRelation(proteincomplex, proteincomplexbiosythesisparticipant)
    .via[Public_ProteinComplex_biosynthesis]((e, s, es) => (es.proteincomplex_id === e.parent_ptr_protein_id, s.id === es.biosynthesis_id))

  // Class Reaction --------------------
  val reaction_map_coordinates = manyToManyRelation(reaction, metabolitemapcoordinate)
    .via[Public_Reaction_map_coordinates]((e, s, es) => (es.reaction_id === e.parent_ptr_species_component_id, s.id === es.reactionmapcoordinate_id))
  val reaction_pathways = manyToManyRelation(reaction, pathway)
    .via[Public_Reaction_pathways]((e, s, es) => (es.reaction_id === e.parent_ptr_species_component_id, s.parent_ptr_species_component_id === es.pathway_id))
  val reaction_coenzymes = manyToManyRelation(reaction, coenzymeparticipant)
    .via[Public_Reaction_coenzymes]((e, s, es) => (es.reaction_id === e.parent_ptr_species_component_id, s.id === es.coenzymeparticipant_id))
  val reaction_stoichiometry = manyToManyRelation(reaction, reactionstoichiometryparticipant)
    .via[Public_Reaction_stoichiometry]((e, s, es) => (es.reaction_id === e.parent_ptr_species_component_id, s.id === es.reactionstoichiometryparticipant_id))

  // Class TranscriptionUnit --------------------
  val transcriptionunit_genes = manyToManyRelation(transcriptionunit, gene)
    .via[Public_TranscriptionUnit_genes]((e, s, es) => (es.transcriptionunit_id === e.parent_ptr_molecule_id, s.parent_ptr_molecule_id === es.gene_id))



  val user = table[Auth_User]
  on(userprofile)(up => declare(up.user_id is unique))
    val userProfileUser = oneToManyRelation(user, userprofile)
    .via((u, up) => u.id === up.user_id)
}