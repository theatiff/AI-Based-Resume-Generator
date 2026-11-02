import React, { useRef } from "react";
import "daisyui/dist/full.css";
import { FaGithub, FaLinkedin, FaPhone, FaEnvelope } from "react-icons/fa";
import { toPng } from "html-to-image";
import { jsPDF } from "jspdf";
import { useReactToPrint } from "react-to-print";

const Resume = ({ data = {} }) => {
  const resumeRef = useRef(null);

  const handleDownloadPdf = () => {
    if (!resumeRef.current) return;
    toPng(resumeRef.current, { quality: 1.0 })
      .then((dataUrl) => {
        const pdf = new jsPDF("p", "mm", "a4");
        pdf.addImage(dataUrl, "PNG", 10, 10, 190, 0);
        pdf.save(`${normalized.personalInformation.fullName || "resume"}.pdf`);
      })
      .catch((err) => console.error("Error generating PDF", err));
  };

  const normalizeData = (d) => ({
    personalInformation: {
      fullName: d?.personalInformation?.fullName ?? d?.name ?? "",
      email: d?.personalInformation?.email ?? "",
      phoneNumber: d?.personalInformation?.phoneNumber ?? "",
      location: d?.personalInformation?.location ?? "",
      gitHub: d?.personalInformation?.gitHub ?? d?.github ?? "",
      linkedIn: d?.personalInformation?.linkedIn ?? d?.linkedIn ?? "",
    },
    summary: d?.summary ?? "",
    skills: d?.skills ?? [],
    experience: d?.experience ?? [],
    education: d?.education ?? [],
    certifications: d?.certifications ?? [],
    projects: d?.projects ?? [],
    achievements: d?.achievements ?? [],
    languages: d?.languages ?? [],
    interests: d?.interests ?? [],
  });

  const normalized = normalizeData(data);

  return (
    <>
      <div
        ref={resumeRef}
        className="max-w-4xl mx-auto shadow-2xl rounded-lg p-8 space-y-6 bg-base-100 text-base-content border border-gray-200 dark:border-gray-700 transition-all duration-300"
      >
        <div className="text-center space-y-2">
          <h1 className="text-4xl font-bold text-primary">{normalized.personalInformation.fullName}</h1>
          <p className="text-lg text-gray-500">{normalized.personalInformation.location}</p>

          <div className="flex justify-center space-x-4 mt-2">
            {normalized.personalInformation.email && (
              <a href={`mailto:${normalized.personalInformation.email}`} className="flex items-center text-secondary hover:underline">
                <FaEnvelope className="mr-2" /> {normalized.personalInformation.email}
              </a>
            )}

            {normalized.personalInformation.phoneNumber && (
              <div className="flex items-center text-secondary">
                <FaPhone className="mr-2" /> {normalized.personalInformation.phoneNumber}
              </div>
            )}

            {normalized.personalInformation.gitHub && (
              <a href={normalized.personalInformation.gitHub} target="_blank" rel="noopener noreferrer" className="text-gray-500 hover:text-gray-700 flex items-center">
                <FaGithub className="mr-2" /> GitHub
              </a>
            )}

            {normalized.personalInformation.linkedIn && (
              <a href={normalized.personalInformation.linkedIn} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:text-blue-700 flex items-center">
                <FaLinkedin className="mr-2" /> LinkedIn
              </a>
            )}
          </div>
        </div>

        <div className="divider" />

        {normalized.summary && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Summary</h2>
            <p className="mt-2 text-gray-700 dark:text-gray-300">{normalized.summary}</p>
          </section>
        )}

        <div className="divider" />

        {normalized.skills.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Skills</h2>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mt-2">
              {normalized.skills.map((skill, i) => (
                <div key={i} className="badge badge-outline badge-lg px-4 py-2">
                  {skill.title} - <span className="ml-1 font-semibold">{skill.level}</span>
                </div>
              ))}
            </div>
          </section>
        )}

        <div className="divider" />

        {normalized.experience.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Experience</h2>
            {normalized.experience.map((exp, i) => (
              <div key={i} className="mb-4 p-4 rounded-lg shadow-md bg-base-200 border border-gray-300 dark:border-gray-700">
                <h3 className="text-xl font-bold">{exp.title || exp.position}</h3>
                <p className="text-gray-500">{exp.company} | {exp.location}</p>
                <p className="text-gray-400">{exp.duration}</p>
                <p className="mt-2 text-gray-600 dark:text-gray-300">{exp.responsibility}</p>
              </div>
            ))}
          </section>
        )}

        <div className="divider" />

        {normalized.education.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Education</h2>
            {normalized.education.map((edu, i) => (
              <div key={i} className="mb-4 p-4 rounded-lg shadow-md bg-base-200 border border-gray-300 dark:border-gray-700">
                <h3 className="text-xl font-bold">{edu.degree || edu.program}</h3>
                <p className="text-gray-500">{edu.university}, {edu.location}</p>
                <p className="text-gray-400">🎓 Graduation Year: {edu.graduationYear}</p>
              </div>
            ))}
          </section>
        )}

        <div className="divider" />

        {normalized.projects.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Projects</h2>
            {normalized.projects.map((proj, i) => (
              <div key={i} className="mb-4 p-4 rounded-lg shadow-md bg-base-200 border border-gray-300 dark:border-gray-700">
                <h3 className="text-xl font-bold">{proj.title}</h3>
                <p className="text-gray-600 dark:text-gray-300">{proj.description}</p>
                {Array.isArray(proj.technologiesUsed) && (
                  <p className="text-gray-500">🛠 Technologies: {proj.technologiesUsed.join(", ")}</p>
                )}
                {proj.githubLink && (
                  <a href={proj.githubLink} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">🔗 GitHub Link</a>
                )}
              </div>
            ))}
          </section>
        )}

        <div className="divider" />

        {normalized.achievements.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Achievements</h2>
            {normalized.achievements.map((ach, i) => (
              <div key={i} className="mb-4 p-4 rounded-lg shadow-md bg-base-200 border border-gray-300 dark:border-gray-700">
                <h3 className="text-xl font-bold">{ach.title}</h3>
                <p className="text-gray-500">{ach.year}</p>
                <p className="text-gray-600 dark:text-gray-300">{ach.extraInformation}</p>
              </div>
            ))}
          </section>
        )}

        <div className="divider" />

        {normalized.languages.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Languages</h2>
            <ul className="list-disc pl-6 text-gray-700 dark:text-gray-300">
              {normalized.languages.map((l, i) => <li key={i}>{l.name || l}</li>)}
            </ul>
          </section>
        )}

        <div className="divider" />

        {normalized.interests.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-secondary">Interests</h2>
            <ul className="list-disc pl-6 text-gray-700 dark:text-gray-300">
              {normalized.interests.map((it, i) => <li key={i}>{it.name || it}</li>)}
            </ul>
          </section>
        )}
      </div>

      <section className="flex justify-center mt-4">
        <button onClick={handleDownloadPdf} className="btn btn-primary">Download PDF</button>
      </section>
    </>
  );
};

export default Resume;
